package client;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class MusicBoxClient {
    private JFrame appFrame;
    private JPanel gridPanel;
    ArrayList<JCheckBox> checkboxes;
    ObjectOutputStream out;
    ObjectInputStream in;
    boolean[] checkboxesState;
    Track track;
    Sequencer sequencer;
    Sequence sequence;
    JTextField userMessage;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
            "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
            "High Agogo", "Open Hi Conga"};

    // value of keys for drum beats, just like for piano the channel code was 44,
    // and 35 is the key for Bass Drum
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    Runnable chatReaderJob = () -> {

    };

    ActionListener startActionListener = e -> buildTrackAndStart();
    ActionListener stopActionListener = e -> sequencer.stop();
    ActionListener tempoUpActionListener = e -> {
            /**
             * tempoFactor value is by default 1.0
             *
             * simply scale up by 3%
             */
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*1.03));
    };
    ActionListener tempDownActionListener = e -> {
        /**
         * tempoFactor value is by default 1.0
         *
         * simply scale down by 3%
         */
        float tempoFactor = sequencer.getTempoFactor();
        sequencer.setTempoFactor((float)(tempoFactor*0.97));
    };
    ActionListener saveBeatActionListener = e -> {
        /**
         * save the beat we just made
         */
        for(int i=0; i<checkboxes.size(); i++) {
            JCheckBox checkBox = checkboxes.get(i);
            if (checkBox.isSelected()) {
                checkboxesState[i]=true;
            }
        }

        try {
            FileOutputStream fileStream = new FileOutputStream(new File("MyBeat.ser"));
            ObjectOutputStream os = new ObjectOutputStream(fileStream);
            os.writeObject(checkboxesState);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    };
    ActionListener restoreBeatActionListener = e -> {
        /**
         * restore a track (beat pattern)
         */
        try {
            FileInputStream fileInputStream = new FileInputStream(new File("MyBeat.ser"));
            ObjectInputStream is = new ObjectInputStream(fileInputStream);
            checkboxesState = (boolean[]) is.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        for (int i=0; i<checkboxesState.length; i++) {
            JCheckBox checkBox = checkboxes.get(i);
            if (checkboxesState[i]) {
                checkBox.setSelected(true);
            } else {
                checkBox.setSelected(false);
            }
        }
//
//            sequencer.stop();
//            buildTrackAndStart();
    };
    ActionListener clearTrackActionListener = e -> {
        /**
         * clear the entire checkboxes and reset checkboxstate and the checkboxlist
         */
        for (int i=0; i<checkboxesState.length; i++) {
            checkboxesState[i]=false;
            JCheckBox checkBox = checkboxes.get(i);
            checkBox.setSelected(false);
        }
        sequencer.stop();
    };
    ActionListener sendTrackActionListener = e -> {
        System.out.println("Sending message (object 1) and beat pattern (object 2) to everyone");

    };

    public static void main(String[] args) {
        MusicBoxClient musicBoxClient = new MusicBoxClient();
        musicBoxClient.startUp();
    }

    public void buildGUI() {
        appFrame = new JFrame("Online Beat Box");
        appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BorderLayout layout = new BorderLayout();

        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // 256 checkboxes for the instruments and their beats (16 x 16)
        checkboxes = new ArrayList<>();

        // declare the checkboxes state to serialize(save) the track
        checkboxesState = new boolean[256]; // 16 x 16

        // create the 4 buttons and put it inside a Swing Box
        Box buttonsBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(startActionListener);
        buttonsBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(stopActionListener);
        buttonsBox.add(stop);

        JButton tempoUp = new JButton("Tempo Up");
        tempoUp.addActionListener(tempoUpActionListener);
        buttonsBox.add(tempoUp);

        JButton tempoDown = new JButton("Tempo Down");
        tempoDown.addActionListener(tempDownActionListener);
        buttonsBox.add(tempoDown);

        JButton saveBeat = new JButton("Save it");
        saveBeat.addActionListener(saveBeatActionListener);
        buttonsBox.add(saveBeat);

        JButton restoreBeat = new JButton("Restore");
        restoreBeat.addActionListener(restoreBeatActionListener);
        buttonsBox.add(restoreBeat);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(clearTrackActionListener);
        buttonsBox.add(clearButton);

        JButton sendButton = new JButton("Send it");
        sendButton.addActionListener(sendTrackActionListener);
        buttonsBox.add(sendButton);

        userMessage = new JTextField();
        buttonsBox.add(userMessage);


        // create 16 labels for the instrument names and put them in another Box
        Box namesBox = new Box(BoxLayout.Y_AXIS);
        for (int i=0; i<16; i++) {
            namesBox.add(new Label(instrumentNames[i]));
        }
        background.add(BorderLayout.WEST, namesBox);
        background.add(BorderLayout.EAST, buttonsBox);


        //create the grid for the checkboxes and the labels to add (16 x 16)
        GridLayout grid = new GridLayout(16,16);
        grid.setHgap(2);
        grid.setVgap(1);
        gridPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, gridPanel);

        // make the checkboxes, set them to false initially and
        // add them to the checkboxes list and to the GUI gridPanel
        for (int i=0; i<256; i++) {
            JCheckBox checkbox = new JCheckBox();
            checkbox.setSelected(false);
            checkboxes.add(checkbox);
            gridPanel.add(checkbox);
        }

        appFrame.getContentPane().add(background);
        appFrame.setBounds(50,50,300,300);
        appFrame.pack();
        appFrame.setVisible(true);
    }

    private void startUp() {
        connectToMusicServer();
        setUpMidi();
        buildGUI();
    }

    private void connectToMusicServer() {
        try {
            Socket socket = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Thread chatReader = new Thread(chatReaderJob);
            chatReader.start();
        } catch (IOException e) {
            System.out.println("sorry could not connect you to the music server, you will have to play it alone");
            e.printStackTrace();
        }

    }



    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();

            sequence = new Sequence(Sequence.PPQ, 4);

            track = sequence.createTrack();

            sequencer.setTempoInBPM(120);
        }catch(MidiUnavailableException e) {
            e.printStackTrace();
        } catch(InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    /**
     * this is where we turn the checkboxes' state into MIDI events and
     * add them to the track
     */
    public void buildTrackAndStart() {
        // make a 16-element array to hold the values for one instrument across
        // all 16-beats
        // if the instrument is supposed to play on that beat, the value at that element will be the key
        // otherwise it will be 0
        int[] trackList = null;

        /**
         * bass drums: [0 - 15]
         * hi hat: [16 - 31]
         * ..
         * ..
         * cymbals: [239 - 255]
         */


        // delete old track and create a new one
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        // for each of the 16 rows
        for (int i=0; i<16; i++) {
            trackList = new int[16]; // new track list is created for each instrument (row)

            int key = instruments[i];

            // for each beat of the instrument (row)
            for (int j=0; j<16; j++) {
                JCheckBox checkedBox = checkboxes.get(j + 16*i);

                if (checkedBox.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }

            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }


        /**
         * make sure that there IS an event at the beat 16 (0 - 15). Otherwise the BeatBox might not go
         * the full 16 beats before it starts over;
         */
        track.add(makeEvent(192,9,1,0,15));


        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
            System.out.println("Tempo: " + sequencer.getTempoFactor());
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public void makeTracks(int[] trackList) {
        /**
         * looping through the trackList to create the actual track for 1 instrument
         */
        for (int i=0; i<trackList.length; i++) {
            int beat = trackList[i];

            //create a MIDI Event
            if (beat != 0) {
                track.add(makeEvent(144, 9, beat, 100, i));
                track.add(makeEvent(128, 9, beat, 100, i+1)); // k -> k+1 means 1 interval
            }
        }
    }

    private MidiEvent makeEvent(int command, int channel, int data1, int data2, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage message = new ShortMessage();
            message.setMessage(command, channel, data1, data2);
            event = new MidiEvent(message,tick);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return event;
    }
}
