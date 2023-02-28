package client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MusicBoxClientTest {

    private MusicBoxClient musicBoxClientUnderTest;

    @BeforeEach
    void setUp() {
        musicBoxClientUnderTest = new MusicBoxClient();
        musicBoxClientUnderTest.checkboxes = new ArrayList<>(List.of(new JCheckBox("text", false)));
        musicBoxClientUnderTest.out = mock(ObjectOutputStream.class);
        musicBoxClientUnderTest.in = mock(ObjectInputStream.class);
        musicBoxClientUnderTest.checkboxesState = new boolean[]{false};
        musicBoxClientUnderTest.track = mock(Track.class);
        musicBoxClientUnderTest.sequencer = mock(Sequencer.class);
        musicBoxClientUnderTest.sequence = mock(Sequence.class);
        musicBoxClientUnderTest.userMessage = mock(JTextField.class);
        musicBoxClientUnderTest.instrumentNames = new String[]{"instrumentNames"};
        musicBoxClientUnderTest.instruments = new int[]{0};
        musicBoxClientUnderTest.chatReaderJob = mock(Runnable.class);
        musicBoxClientUnderTest.startActionListener = mock(ActionListener.class);
        musicBoxClientUnderTest.stopActionListener = mock(ActionListener.class);
        musicBoxClientUnderTest.tempoUpActionListener = mock(ActionListener.class);
        musicBoxClientUnderTest.tempDownActionListener = mock(ActionListener.class);
        musicBoxClientUnderTest.saveBeatActionListener = mock(ActionListener.class);
        musicBoxClientUnderTest.restoreBeatActionListener = mock(ActionListener.class);
        musicBoxClientUnderTest.clearTrackActionListener = mock(ActionListener.class);
        musicBoxClientUnderTest.sendTrackActionListener = mock(ActionListener.class);
    }

    @Test
    void testBuildGUI() {
        // Setup
        // Run the test
        musicBoxClientUnderTest.buildGUI();

        // Verify the results
    }

    @Test
    void testSetUpMidi() throws MidiUnavailableException {
        // Setup
        when(musicBoxClientUnderTest.sequence.createTrack()).thenReturn(null);

        // Run the test
        musicBoxClientUnderTest.setUpMidi();

        // Verify the results
        verify(musicBoxClientUnderTest.sequencer).open();
        verify(musicBoxClientUnderTest.sequencer).setTempoInBPM(120);
    }

    @Test
    void testSetUpMidi_SequencerOpenThrowsMidiUnavailableException() throws MidiUnavailableException {
        // Setup
        doThrow(MidiUnavailableException.class).when(musicBoxClientUnderTest.sequencer).open();

        // Run the test
        musicBoxClientUnderTest.setUpMidi();

        // Verify the results
    }

    @Test
    void testBuildTrackAndStart() throws InvalidMidiDataException {
        // Setup
        when(musicBoxClientUnderTest.sequence.deleteTrack(any(Track.class))).thenReturn(false);
        when(musicBoxClientUnderTest.sequence.createTrack()).thenReturn(null);
        when(musicBoxClientUnderTest.track.add(any(MidiEvent.class))).thenReturn(false);
        when(musicBoxClientUnderTest.sequencer.getTempoFactor()).thenReturn(0.0f);

        // Run the test
        musicBoxClientUnderTest.buildTrackAndStart();

        // Verify the results
        verify(musicBoxClientUnderTest.sequence).deleteTrack(any(Track.class));
        verify(musicBoxClientUnderTest.track).add(any(MidiEvent.class));
        verify(musicBoxClientUnderTest.sequencer).setSequence(any(Sequence.class));
        verify(musicBoxClientUnderTest.sequencer).setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        verify(musicBoxClientUnderTest.sequencer).start();
        verify(musicBoxClientUnderTest.sequencer).setTempoInBPM(120);
    }

    @Test
    void testBuildTrackAndStart_SequencerSetSequenceThrowsInvalidMidiDataException() throws InvalidMidiDataException {
        // Setup
        when(musicBoxClientUnderTest.sequence.deleteTrack(any(Track.class))).thenReturn(false);
        when(musicBoxClientUnderTest.sequence.createTrack()).thenReturn(null);
        when(musicBoxClientUnderTest.track.add(any(MidiEvent.class))).thenReturn(false);
        doThrow(InvalidMidiDataException.class).when(musicBoxClientUnderTest.sequencer).setSequence(
                any(Sequence.class));

        // Run the test
        musicBoxClientUnderTest.buildTrackAndStart();

        // Verify the results
        verify(musicBoxClientUnderTest.sequence).deleteTrack(any(Track.class));
        verify(musicBoxClientUnderTest.track).add(any(MidiEvent.class));
    }

    @Test
    void testMakeTracks() {
        // Setup
        when(musicBoxClientUnderTest.track.add(any(MidiEvent.class))).thenReturn(false);

        // Run the test
        musicBoxClientUnderTest.makeTracks(new int[]{0});

        // Verify the results
        verify(musicBoxClientUnderTest.track).add(any(MidiEvent.class));
    }

    @Test
    void testMain() {
        // Setup
        // Run the test
        MusicBoxClient.main(new String[]{"args"});

        // Verify the results
    }
}
