import javax.media.\*;

import javax.swing.\*;

import java.awt.\*;

import java.io.\*;

import java.util.\*;

public class MediaPlayer extends JFrame implements ControllerListener {

private Player player;

private Component videoComponent;

private Component controlComponent;

private JPanel lyricsPanel;

private JTextArea lyricsArea;

private Map\<Long, String\> lyrics;

private Timer lyricsTimer;

public MediaPlayer() {

setTitle(\"Media Player\");

setLayout(new BorderLayout());

initComponents();

setDefaultCloseOperation(JFrame.EXIT\_ON\_CLOSE);

setSize(800, 600);

}

private void initComponents() {

// Media panel

JPanel mediaPanel = new JPanel(new BorderLayout());

add(mediaPanel, BorderLayout.CENTER);

// Lyrics panel

lyricsPanel = new JPanel(new BorderLayout());

lyricsArea = new JTextArea();

lyricsArea.setEditable(false);

lyricsPanel.add(new JScrollPane(lyricsArea), BorderLayout.CENTER);

add(lyricsPanel, BorderLayout.EAST);

lyricsPanel.setPreferredSize(new Dimension(200, 0));

lyricsPanel.setVisible(false);

}

public void loadMedia(File mediaFile) throws Exception {

if (player != null) {

player.stop();

player.close();

}

MediaLocator locator = new MediaLocator(mediaFile.toURI().toURL());

player = Manager.createPlayer(locator);

player.addControllerListener(this);

player.prefetch();

if (mediaFile.getName().endsWith(\".mp3\")) {

loadLyrics(mediaFile);

lyricsPanel.setVisible(true);

} else {

lyricsPanel.setVisible(false);

}

}

private void loadLyrics(File mediaFile) {

lyrics = new TreeMap\<\>();

String lyricsPath = mediaFile.getPath().replace(\".mp3\", \".lrc\");

try (BufferedReader reader = new BufferedReader(new
FileReader(lyricsPath))) {

String line;

while ((line = reader.readLine()) != null) {

if (line.matches(\"\\\\\[\\\\d{2}:\\\\d{2}\\\\.\\\\d{2}\\\\\].\*\")) {

long time = parseLyricsTime(line.substring(1, 9));

String text = line.substring(10);

lyrics.put(time, text);

}

}

startLyricsTimer();

} catch (IOException e) {

lyricsArea.setText(\"No lyrics found\");

}

}

private long parseLyricsTime(String timeStr) {

String\[\] parts = timeStr.split(\"\[:.\]\");

return Long.parseLong(parts\[0\]) \* 60000 +

Long.parseLong(parts\[1\]) \* 1000 +

Long.parseLong(parts\[2\]) \* 10;

}

private void startLyricsTimer() {

if (lyricsTimer != null) {

lyricsTimer.stop();

}

lyricsTimer = new Timer(100, e -\> updateLyrics());

lyricsTimer.start();

}

private void updateLyrics() {

if (player != null && lyrics != null) {

long time = player.getMediaTime().getSeconds() \* 1000;

String currentLyric = lyrics.entrySet().stream()

.filter(entry -\> entry.getKey() \<= time)

.reduce((first, second) -\> second)

.map(Map.Entry::getValue)

.orElse(\"\");

lyricsArea.setText(currentLyric);

}

}

\@Override

public void controllerUpdate(ControllerEvent event) {

if (event instanceof RealizeCompleteEvent) {

videoComponent = player.getVisualComponent();

controlComponent = player.getControlPanelComponent();

if (videoComponent != null) {

add(videoComponent, BorderLayout.CENTER);

}

if (controlComponent != null) {

add(controlComponent, BorderLayout.SOUTH);

}

pack();

player.start();

}

}

public static void main(String\[\] args) {

SwingUtilities.invokeLater(() -\> {

MediaPlayer player = new MediaPlayer();

player.setVisible(true);

});

}

}

使用方法：

java

Copy

MediaPlayer player = new MediaPlayer();player.loadMedia(new
File(\"path/to/media\"));

import javax.media.\*;

import javax.swing.\*;

import java.awt.\*;

import java.io.\*;

import java.util.\*;

import java.util.List;

import javax.swing.border.\*;

public class AdvancedMediaPlayer extends JFrame {

private Player player;

private PlaylistManager playlistManager;

private LyricsManager lyricsManager;

private CustomControlPanel controlPanel;

private Component videoComponent;

private JPanel mainPanel, playlistPanel;

private JSplitPane splitPane;

private boolean isFullScreen = false;

// UI Components

private JList\<String\> playlist;

private DefaultListModel\<String\> playlistModel;

public AdvancedMediaPlayer() {

setTitle(\"Advanced Media Player\");

initComponents();

setupLayout();

initPlaylistManager();

initLyricsManager();

setDefaultCloseOperation(JFrame.EXIT\_ON\_CLOSE);

setSize(1024, 768);

}

private void initComponents() {

mainPanel = new JPanel(new BorderLayout());

playlistPanel = new JPanel(new BorderLayout());

playlistModel = new DefaultListModel\<\>();

playlist = new JList\<\>(playlistModel);

controlPanel = new CustomControlPanel(this);

// Setup playlist

playlist.setSelectionMode(ListSelectionModel.SINGLE\_SELECTION);

playlist.addListSelectionListener(e -\> {

if (!e.getValueIsAdjusting()) {

playlistManager.playSelected(playlist.getSelectedIndex());

}

});

JScrollPane playlistScroll = new JScrollPane(playlist);

playlistPanel.add(new JLabel(\" Playlist\", SwingConstants.CENTER),
BorderLayout.NORTH);

playlistPanel.add(playlistScroll, BorderLayout.CENTER);

// Add drag & drop support

new FileDrop(playlist, files -\> {

for (File file : files) {

playlistManager.addFile(file);

}

});

}

private void setupLayout() {

splitPane = new JSplitPane(JSplitPane.HORIZONTAL\_SPLIT, mainPanel,
playlistPanel);

splitPane.setResizeWeight(0.8);

add(splitPane);

mainPanel.add(controlPanel, BorderLayout.SOUTH);

}

// Custom control panel implementation

private class CustomControlPanel extends JPanel {

private JSlider progressSlider, volumeSlider;

private JButton playButton, stopButton, prevButton, nextButton;

private JToggleButton muteButton, fullscreenButton;

private JLabel timeLabel;

public CustomControlPanel(AdvancedMediaPlayer parent) {

setLayout(new BorderLayout());

setBorder(new EmptyBorder(5, 5, 5, 5));

// Create controls

progressSlider = new JSlider(0, 100, 0);

volumeSlider = new JSlider(0, 100, 80);

playButton = new JButton(\"Play\");

stopButton = new JButton(\"Stop\");

prevButton = new JButton(\"Prev\");

nextButton = new JButton(\"Next\");

muteButton = new JToggleButton(\"Mute\");

fullscreenButton = new JToggleButton(\"Full Screen\");

timeLabel = new JLabel(\"00:00 / 00:00\");

// Layout controls

JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5,
0));

buttonPanel.add(prevButton);

buttonPanel.add(playButton);

buttonPanel.add(stopButton);

buttonPanel.add(nextButton);

buttonPanel.add(muteButton);

buttonPanel.add(volumeSlider);

buttonPanel.add(fullscreenButton);

add(progressSlider, BorderLayout.NORTH);

add(buttonPanel, BorderLayout.CENTER);

add(timeLabel, BorderLayout.EAST);

// Add listeners

setupControlListeners();

}

private void setupControlListeners() {

playButton.addActionListener(e -\> togglePlayPause());

stopButton.addActionListener(e -\> stop());

prevButton.addActionListener(e -\> playlistManager.playPrevious());

nextButton.addActionListener(e -\> playlistManager.playNext());

muteButton.addActionListener(e -\> toggleMute());

fullscreenButton.addActionListener(e -\> toggleFullScreen());

volumeSlider.addChangeListener(e -\> {

if (player != null) {

player.getGainControl().setLevel(volumeSlider.getValue() / 100f);

}

});

progressSlider.addMouseListener(new java.awt.event.MouseAdapter() {

public void mouseClicked(java.awt.event.MouseEvent e) {

if (player != null) {

int value = progressSlider.getValueForLocation(e.getX());

seek(value);

}

}

});

}

}

// Playlist Manager implementation

private class PlaylistManager {

private List\<File\> mediaFiles;

private int currentIndex = -1;

public PlaylistManager() {

mediaFiles = new ArrayList\<\>();

}

public void addFile(File file) {

mediaFiles.add(file);

playlistModel.addElement(file.getName());

if (currentIndex == -1) {

playSelected(0);

}

}

public void playSelected(int index) {

if (index \>= 0 && index \< mediaFiles.size()) {

currentIndex = index;

try {

loadMedia(mediaFiles.get(currentIndex));

} catch (Exception e) {

showError(\"Error playing media: \" + e.getMessage());

}

}

}

public void playNext() {

if (currentIndex \< mediaFiles.size() - 1) {

playSelected(currentIndex + 1);

}

}

public void playPrevious() {

if (currentIndex \> 0) {

playSelected(currentIndex - 1);

}

}

}

// Lyrics Manager implementation

private class LyricsManager {

private Map\<Long, String\> lyrics;

private JTextArea lyricsArea;

private Timer lyricsTimer;

private long currentTime = 0;

public LyricsManager() {

lyrics = new TreeMap\<\>();

lyricsArea = new JTextArea();

lyricsArea.setEditable(false);

lyricsArea.setFont(new Font(\"Arial\", Font.PLAIN, 14));

lyricsArea.setLineWrap(true);

lyricsArea.setWrapStyleWord(true);

JScrollPane scrollPane = new JScrollPane(lyricsArea);

scrollPane.setPreferredSize(new Dimension(200, 0));

mainPanel.add(scrollPane, BorderLayout.EAST);

}

public void loadLyrics(File mediaFile) {

lyrics.clear();

String lyricsPath = mediaFile.getPath().replaceAll(\"\\\\.\[\^.\]+\$\",
\".lrc\");

File lyricsFile = new File(lyricsPath);

if (lyricsFile.exists()) {

try (BufferedReader reader = new BufferedReader(new
FileReader(lyricsFile))) {

String line;

while ((line = reader.readLine()) != null) {

parseLyricsLine(line);

}

startLyricsTimer();

} catch (IOException e) {

showError(\"Error loading lyrics: \" + e.getMessage());

}

}

}

private void parseLyricsLine(String line) {

// Support multiple time tags per line

String\[\] parts = line.split(\"\\\\\[\|\\\\\]\");

String text = parts\[parts.length - 1\].trim();

for (int i = 0; i \< parts.length - 1; i++) {

String timeStr = parts\[i\];

if (timeStr.matches(\"\\\\d{2}:\\\\d{2}\\\\.\\\\d{2}\")) {

long time = parseLyricsTime(timeStr);

lyrics.put(time, text);

}

}

}

private void updateLyrics(long time) {

currentTime = time;

String currentLyric = lyrics.entrySet().stream()

.filter(entry -\> entry.getKey() \<= time)

.reduce((first, second) -\> second)

.map(Map.Entry::getValue)

.orElse(\"\");

SwingUtilities.invokeLater(() -\> {

lyricsArea.setText(currentLyric);

lyricsArea.setCaretPosition(0);

});

}

}

// Utility methods

private void loadMedia(File file) throws Exception {

if (player != null) {

player.stop();

player.close();

}

MediaLocator locator = new MediaLocator(file.toURI().toURL());

player = Manager.createPlayer(locator);

player.addControllerListener(new MediaController());

player.prefetch();

if (file.getName().toLowerCase().endsWith(\".mp3\")) {

lyricsManager.loadLyrics(file);

}

}

private void togglePlayPause() {

if (player != null) {

if (player.getState() == Controller.Started) {

player.stop();

} else {

player.start();

}

}

}

private void stop() {

if (player != null) {

player.stop();

player.setMediaTime(new Time(0));

}

}

private void seek(int percentage) {

if (player != null) {

Time duration = player.getDuration();

if (duration != Time.DURATION\_UNKNOWN) {

long seekTime = duration.getNanoseconds() \* percentage / 100;

player.setMediaTime(new Time(seekTime));

}

}

}

private void toggleMute() {

if (player != null && player.getGainControl() != null) {

GainControl gainControl = player.getGainControl();

gainControl.setMute(!gainControl.getMute());

}

}

private void toggleFullScreen() {

if (videoComponent != null) {

if (!isFullScreen) {

dispose();

setUndecorated(true);

setExtendedState(JFrame.MAXIMIZED\_BOTH);

mainPanel.remove(controlPanel);

setVisible(true);

} else {

dispose();

setUndecorated(false);

setExtendedState(JFrame.NORMAL);

mainPanel.add(controlPanel, BorderLayout.SOUTH);

setVisible(true);

}

isFullScreen = !isFullScreen;

}

}

private void showError(String message) {

JOptionPane.showMessageDialog(this, message, \"Error\",
JOptionPane.ERROR\_MESSAGE);

}

private static class MediaController implements ControllerListener {

\@Override

public void controllerUpdate(ControllerEvent event) {

if (event instanceof RealizeCompleteEvent) {

Player player = (Player)event.getSourceController();

Component video = player.getVisualComponent();

if (video != null) {

// Handle video component

}

player.start();

}

}

}

public static void main(String\[\] args) {

try {

UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

} catch (Exception e) {

e.printStackTrace();

}

SwingUtilities.invokeLater(() -\> {

new AdvancedMediaPlayer().setVisible(true);

});

}

}

// FileDrop utility class for drag & drop support

class FileDrop {

public FileDrop(Component c, FileDropListener listener) {

c.setDropTarget(new DropTarget(c, DnDConstants.ACTION\_COPY,

new DropTargetAdapter() {

\@Override

public void drop(DropTargetDropEvent dtde) {

try {

dtde.acceptDrop(DnDConstants.ACTION\_COPY);

List\<File\> files = (List\<File\>)

dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

listener.filesDropped(files.toArray(new File\[0\]));

dtde.dropComplete(true);

} catch (Exception e) {

dtde.rejectDrop();

}

}

}, true));

}

public interface FileDropListener {

void filesDropped(File\[\] files);

}

}

要使用这个播放器，只需要：

java

Copy

public static void main(String\[\] args) { SwingUtilities.invokeLater(()
-\> { AdvancedMediaPlayer player = new AdvancedMediaPlayer();
player.setVisible(true); });}

mediaplayer/

│

├── src/

│ └── com/

│ └── mediaplayer/

│ ├── Main.java

│ │

│ ├── application/

│ │ └── AdvancedMediaPlayer.java

│ │

│ ├── controller/

│ │ ├── MediaController.java

│ │ └── PlaybackController.java

│ │

│ ├── model/

│ │ ├── MediaFile.java

│ │ ├── Playlist.java

│ │ └── LyricsData.java

│ │

│ ├── ui/

│ │ ├── components/

│ │ │ ├── CustomControlPanel.java

│ │ │ ├── PlaylistPanel.java

│ │ │ ├── LyricsPanel.java

│ │ │ ├── MediaPanel.java

│ │ │ └── VolumeSlider.java

│ │ │

│ │ └── dialog/

│ │ ├── MediaInfoDialog.java

│ │ └── PreferencesDialog.java

│ │

│ ├── service/

│ │ ├── MediaPlayerService.java

│ │ ├── PlaylistManager.java

│ │ └── LyricsManager.java

│ │

│ ├── util/

│ │ ├── FileUtils.java

│ │ ├── TimeUtils.java

│ │ ├── LyricsParser.java

│ │ └── FileDrop.java

│ │

│ └── config/

│ └── PlayerConfig.java

│

├── resources/

│ ├── images/

│ │ ├── icons/

│ │ │ ├── play.png

│ │ │ ├── pause.png

│ │ │ ├── stop.png

│ │ │ └── \...

│ │ └── logo.png

│ │

│ └── config/

│ └── default-settings.properties

│

├── lib/

│ ├── jmf.jar

│ └── other-dependencies.jar

│

├── test/

│ └── com/

│ └── mediaplayer/

│ ├── model/

│ │ └── PlaylistTest.java

│ ├── service/

│ │ └── LyricsManagerTest.java

│ └── util/

│ └── TimeUtilsTest.java

│

├── docs/

│ ├── README.md

│ └── API.md

│

└── build/

├── classes/

└── dist/

// model/MediaFile.java

package com.mediaplayer.model;

import java.io.File;

import java.util.Objects;

public class MediaFile {

private File file;

private MediaType type;

private String title;

private long duration;

private File lyricsFile;

public enum MediaType {

AUDIO, VIDEO, IMAGE

}

public MediaFile(File file) {

this.file = file;

this.title = file.getName();

this.type = determineType(file);

}

private MediaType determineType(File file) {

String name = file.getName().toLowerCase();

if (name.endsWith(\".mp3\") \|\| name.endsWith(\".wav\")) {

return MediaType.AUDIO;

} else if (name.endsWith(\".mp4\") \|\| name.endsWith(\".avi\")) {

return MediaType.VIDEO;

} else {

return MediaType.IMAGE;

}

}

// Getters and setters

public File getFile() { return file; }

public MediaType getType() { return type; }

public String getTitle() { return title; }

public void setTitle(String title) { this.title = title; }

public long getDuration() { return duration; }

public void setDuration(long duration) { this.duration = duration; }

public File getLyricsFile() { return lyricsFile; }

public void setLyricsFile(File lyricsFile) { this.lyricsFile =
lyricsFile; }

\@Override

public boolean equals(Object o) {

if (this == o) return true;

if (o == null \|\| getClass() != o.getClass()) return false;

MediaFile mediaFile = (MediaFile) o;

return Objects.equals(file, mediaFile.file);

}

\@Override

public int hashCode() {

return Objects.hash(file);

}

}

// model/Playlist.java

package com.mediaplayer.model;

import java.util.\*;

import java.io.\*;

public class Playlist implements Serializable {

private String name;

private List\<MediaFile\> mediaFiles;

private int currentIndex;

private boolean repeat;

private boolean shuffle;

public Playlist(String name) {

this.name = name;

this.mediaFiles = new ArrayList\<\>();

this.currentIndex = -1;

this.repeat = false;

this.shuffle = false;

}

public void addMedia(MediaFile file) {

mediaFiles.add(file);

if (currentIndex == -1) currentIndex = 0;

}

public void removeMedia(int index) {

if (index \>= 0 && index \< mediaFiles.size()) {

mediaFiles.remove(index);

if (mediaFiles.isEmpty()) currentIndex = -1;

else if (index \<= currentIndex) currentIndex\--;

}

}

public MediaFile getCurrentMedia() {

return currentIndex \>= 0 ? mediaFiles.get(currentIndex) : null;

}

public MediaFile next() {

if (mediaFiles.isEmpty()) return null;

if (shuffle) {

currentIndex = new Random().nextInt(mediaFiles.size());

} else {

currentIndex = (currentIndex + 1) % mediaFiles.size();

if (currentIndex == 0 && !repeat) return null;

}

return getCurrentMedia();

}

public MediaFile previous() {

if (mediaFiles.isEmpty()) return null;

currentIndex = (currentIndex - 1 + mediaFiles.size()) %
mediaFiles.size();

return getCurrentMedia();

}

// Getters and setters

public String getName() { return name; }

public void setName(String name) { this.name = name; }

public List\<MediaFile\> getMediaFiles() { return mediaFiles; }

public boolean isRepeat() { return repeat; }

public void setRepeat(boolean repeat) { this.repeat = repeat; }

public boolean isShuffle() { return shuffle; }

public void setShuffle(boolean shuffle) { this.shuffle = shuffle; }

}

// model/LyricsData.java

package com.mediaplayer.model;

import java.util.\*;

public class LyricsData {

private TreeMap\<Long, String\> lyrics;

private String title;

private String artist;

private String album;

public LyricsData() {

lyrics = new TreeMap\<\>();

}

public void addLyric(long timeMs, String text) {

lyrics.put(timeMs, text);

}

public String getLyricAt(long timeMs) {

Map.Entry\<Long, String\> entry = lyrics.floorEntry(timeMs);

return entry != null ? entry.getValue() : \"\";

}

public void clear() {

lyrics.clear();

}

// Getters and setters

public Map\<Long, String\> getLyrics() { return lyrics; }

public String getTitle() { return title; }

public void setTitle(String title) { this.title = title; }

public String getArtist() { return artist; }

public void setArtist(String artist) { this.artist = artist; }

public String getAlbum() { return album; }

public void setAlbum(String album) { this.album = album; }

}

// util/TimeUtils.java

package com.mediaplayer.util;

public class TimeUtils {

public static String formatTime(long milliseconds) {

long seconds = milliseconds / 1000;

long minutes = seconds / 60;

seconds = seconds % 60;

return String.format(\"%02d:%02d\", minutes, seconds);

}

public static long parseLyricTime(String timeStr) {

String\[\] parts = timeStr.split(\":\|\\\\.\");

if (parts.length != 3) throw new IllegalArgumentException(\"Invalid time
format\");

return Long.parseLong(parts\[0\]) \* 60000 +

Long.parseLong(parts\[1\]) \* 1000 +

Long.parseLong(parts\[2\]) \* 10;

}

}

// util/FileUtils.java

package com.mediaplayer.util;

import java.io.\*;

import com.mediaplayer.model.MediaFile;

public class FileUtils {

public static final String\[\] SUPPORTED\_AUDIO = {\".mp3\", \".wav\",
\".aac\"};

public static final String\[\] SUPPORTED\_VIDEO = {\".mp4\", \".avi\",
\".mkv\"};

public static final String\[\] SUPPORTED\_IMAGE = {\".jpg\", \".png\",
\".gif\"};

public static boolean isSupported(File file) {

String name = file.getName().toLowerCase();

for (String ext : SUPPORTED\_AUDIO) {

if (name.endsWith(ext)) return true;

}

for (String ext : SUPPORTED\_VIDEO) {

if (name.endsWith(ext)) return true;

}

for (String ext : SUPPORTED\_IMAGE) {

if (name.endsWith(ext)) return true;

}

return false;

}

public static File findLyricsFile(MediaFile mediaFile) {

String path = mediaFile.getFile().getAbsolutePath();

String lyricsPath = path.substring(0, path.lastIndexOf(\".\")) +
\".lrc\";

File lyricsFile = new File(lyricsPath);

return lyricsFile.exists() ? lyricsFile : null;

}

public static void savePlaylist(File file, List\<String\> paths) throws
IOException {

try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

for (String path : paths) {

writer.write(path);

writer.newLine();

}

}

}

public static List\<String\> loadPlaylist(File file) throws IOException
{

List\<String\> paths = new ArrayList\<\>();

try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

String line;

while ((line = reader.readLine()) != null) {

paths.add(line.trim());

}

}

return paths;

}

}

// util/LyricsParser.java

package com.mediaplayer.util;

import java.io.\*;

import com.mediaplayer.model.LyricsData;

public class LyricsParser {

public static LyricsData parse(File file) throws IOException {

LyricsData lyricsData = new LyricsData();

try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

String line;

while ((line = reader.readLine()) != null) {

parseLine(line, lyricsData);

}

}

return lyricsData;

}

private static void parseLine(String line, LyricsData lyricsData) {

// 处理标签信息

if (line.startsWith(\"\[ti:\")) {

lyricsData.setTitle(line.substring(4, line.length() - 1));

return;

}

if (line.startsWith(\"\[ar:\")) {

lyricsData.setArtist(line.substring(4, line.length() - 1));

return;

}

if (line.startsWith(\"\[al:\")) {

lyricsData.setAlbum(line.substring(4, line.length() - 1));

return;

}

// 处理时间标签

String\[\] parts = line.split(\"\\\\\[\|\\\\\]\");

String text = parts\[parts.length - 1\].trim();

for (int i = 0; i \< parts.length - 1; i++) {

String timeStr = parts\[i\];

if (timeStr.matches(\"\\\\d{2}:\\\\d{2}\\\\.\\\\d{2}\")) {

long time = TimeUtils.parseLyricTime(timeStr);

lyricsData.addLyric(time, text);

}

}

}

}

// service/MediaPlayerService.java

package com.mediaplayer.service;

import javax.media.\*;

import com.mediaplayer.model.\*;

import java.io.File;

import java.util.EventListener;

public class MediaPlayerService {

private Player player;

private MediaFile currentMedia;

private PlayerState state;

private float volume = 1.0f;

private boolean muted = false;

public enum PlayerState {

UNINITIATED, PREPARING, READY, PLAYING, PAUSED, STOPPED

}

public interface PlayerEventListener extends EventListener {

void onStateChanged(PlayerState newState);

void onProgress(long current, long total);

void onMediaChanged(MediaFile media);

void onError(String message);

}

private List\<PlayerEventListener\> listeners = new ArrayList\<\>();

public void addListener(PlayerEventListener listener) {

listeners.add(listener);

}

public void loadMedia(MediaFile media) throws Exception {

if (player != null) {

player.stop();

player.close();

}

currentMedia = media;

MediaLocator locator = new
MediaLocator(media.getFile().toURI().toURL());

player = Manager.createPlayer(locator);

player.addControllerListener(new PlayerController());

setState(PlayerState.PREPARING);

player.prefetch();

notifyMediaChanged(media);

}

public void play() {

if (player != null && state != PlayerState.PLAYING) {

player.start();

setState(PlayerState.PLAYING);

}

}

public void pause() {

if (player != null && state == PlayerState.PLAYING) {

player.stop();

setState(PlayerState.PAUSED);

}

}

public void stop() {

if (player != null) {

player.stop();

player.setMediaTime(new Time(0));

setState(PlayerState.STOPPED);

}

}

public void seek(long position) {

if (player != null) {

player.setMediaTime(new Time(position));

}

}

public void setVolume(float volume) {

this.volume = volume;

if (player != null && player.getGainControl() != null) {

player.getGainControl().setLevel(muted ? 0 : volume);

}

}

public void setMuted(boolean muted) {

this.muted = muted;

if (player != null && player.getGainControl() != null) {

player.getGainControl().setLevel(muted ? 0 : volume);

}

}

private void setState(PlayerState newState) {

state = newState;

notifyStateChanged(newState);

}

private void notifyStateChanged(PlayerState state) {

for (PlayerEventListener listener : listeners) {

listener.onStateChanged(state);

}

}

private void notifyProgress(long current, long total) {

for (PlayerEventListener listener : listeners) {

listener.onProgress(current, total);

}

}

private void notifyMediaChanged(MediaFile media) {

for (PlayerEventListener listener : listeners) {

listener.onMediaChanged(media);

}

}

private void notifyError(String message) {

for (PlayerEventListener listener : listeners) {

listener.onError(message);

}

}

private class PlayerController implements ControllerListener {

\@Override

public void controllerUpdate(ControllerEvent event) {

if (event instanceof RealizeCompleteEvent) {

setState(PlayerState.READY);

} else if (event instanceof EndOfMediaEvent) {

stop();

} else if (event instanceof ControllerErrorEvent) {

notifyError(((ControllerErrorEvent)event).getMessage());

}

}

}

}

// ui/components/CustomControlPanel.java

package com.mediaplayer.ui.components;

import javax.swing.\*;

import java.awt.\*;

import java.awt.event.\*;

import com.mediaplayer.service.MediaPlayerService;

import com.mediaplayer.util.TimeUtils;

public class CustomControlPanel extends JPanel {

private MediaPlayerService playerService;

private JButton playButton, stopButton, prevButton, nextButton;

private JToggleButton muteButton;

private VolumeSlider volumeSlider;

private JSlider progressSlider;

private JLabel timeLabel;

private Timer updateTimer;

public CustomControlPanel(MediaPlayerService playerService) {

this.playerService = playerService;

initComponents();

setupLayout();

setupListeners();

startUpdateTimer();

}

private void initComponents() {

playButton = new JButton(new
ImageIcon(\"resources/images/icons/play.png\"));

stopButton = new JButton(new
ImageIcon(\"resources/images/icons/stop.png\"));

prevButton = new JButton(new
ImageIcon(\"resources/images/icons/previous.png\"));

nextButton = new JButton(new
ImageIcon(\"resources/images/icons/next.png\"));

muteButton = new JToggleButton(new
ImageIcon(\"resources/images/icons/volume.png\"));

volumeSlider = new VolumeSlider();

progressSlider = new JSlider(0, 100, 0);

timeLabel = new JLabel(\"00:00 / 00:00\");

// Set tooltips

playButton.setToolTipText(\"Play/Pause\");

stopButton.setToolTipText(\"Stop\");

prevButton.setToolTipText(\"Previous\");

nextButton.setToolTipText(\"Next\");

muteButton.setToolTipText(\"Mute\");

}

private void setupLayout() {

setLayout(new BorderLayout(5, 5));

setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

// Progress panel

JPanel progressPanel = new JPanel(new BorderLayout(5, 0));

progressPanel.add(progressSlider, BorderLayout.CENTER);

progressPanel.add(timeLabel, BorderLayout.EAST);

// Control buttons panel

JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5,
0));

controlPanel.add(prevButton);

controlPanel.add(playButton);

controlPanel.add(stopButton);

controlPanel.add(nextButton);

// Volume control panel

JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

volumePanel.add(muteButton);

volumePanel.add(volumeSlider);

// Main panel

add(progressPanel, BorderLayout.NORTH);

add(controlPanel, BorderLayout.CENTER);

add(volumePanel, BorderLayout.EAST);

}

private void setupListeners() {

playButton.addActionListener(e -\> playerService.play());

stopButton.addActionListener(e -\> playerService.stop());

muteButton.addActionListener(e -\>
playerService.setMuted(muteButton.isSelected()));

volumeSlider.addChangeListener(e -\>

playerService.setVolume(volumeSlider.getValue() / 100f));

progressSlider.addMouseListener(new MouseAdapter() {

\@Override

public void mouseClicked(MouseEvent e) {

int value = progressSlider.getValueForXPosition(e.getX());

// Convert to media time and seek

}

});

}

private void startUpdateTimer() {

updateTimer = new Timer(100, e -\> updateProgress());

updateTimer.start();

}

private void updateProgress() {

// Update progress slider and time label

}

}

// ui/components/PlaylistPanel.java

package com.mediaplayer.ui.components;

import javax.swing.\*;

import java.awt.\*;

import com.mediaplayer.model.\*;

import java.util.List;

public class PlaylistPanel extends JPanel {

private JList\<MediaFile\> playlistList;

private DefaultListModel\<MediaFile\> listModel;

private Playlist playlist;

private JPopupMenu contextMenu;

public PlaylistPanel() {

initComponents();

setupLayout();

setupContextMenu();

}

private void initComponents() {

listModel = new DefaultListModel\<\>();

playlistList = new JList\<\>(listModel);

playlistList.setCellRenderer(new MediaFileCellRenderer());

playlistList.setSelectionMode(ListSelectionModel.SINGLE\_SELECTION);

}

private void setupLayout() {

setLayout(new BorderLayout());

add(new JScrollPane(playlistList), BorderLayout.CENTER);

JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

buttonPanel.add(new JButton(\"Add\"));

buttonPanel.add(new JButton(\"Remove\"));

add(buttonPanel, BorderLayout.SOUTH);

}

private void setupContextMenu() {

contextMenu = new JPopupMenu();

JMenuItem removeItem = new JMenuItem(\"Remove\");

JMenuItem propertiesItem = new JMenuItem(\"Properties\");

removeItem.addActionListener(e -\> removeSelected());

propertiesItem.addActionListener(e -\> showProperties());

contextMenu.add(removeItem);

contextMenu.add(propertiesItem);

playlistList.addMouseListener(new MouseAdapter() {

\@Override

public void mouseReleased(MouseEvent e) {

if (e.isPopupTrigger()) {

contextMenu.show(e.getComponent(), e.getX(), e.getY());

}

}

});

}

public void setPlaylist(Playlist playlist) {

this.playlist = playlist;

updateList();

}

private void updateList() {

listModel.clear();

for (MediaFile file : playlist.getMediaFiles()) {

listModel.addElement(file);

}

}

private class MediaFileCellRenderer extends DefaultListCellRenderer {

\@Override

public Component getListCellRendererComponent(

JList\<?\> list, Object value, int index,

boolean isSelected, boolean cellHasFocus) {

super.getListCellRendererComponent(list, value, index, isSelected,
cellHasFocus);

if (value instanceof MediaFile) {

MediaFile file = (MediaFile) value;

setText(file.getTitle());

setIcon(getIconForType(file.getType()));

}

return this;

}

private Icon getIconForType(MediaFile.MediaType type) {

switch (type) {

case AUDIO: return new ImageIcon(\"resources/images/icons/audio.png\");

case VIDEO: return new ImageIcon(\"resources/images/icons/video.png\");

case IMAGE: return new ImageIcon(\"resources/images/icons/image.png\");

default: return null;

}

}

}

}

// ui/components/LyricsPanel.java

package com.mediaplayer.ui.components;

import javax.swing.\*;

import java.awt.\*;

import com.mediaplayer.model.LyricsData;

public class LyricsPanel extends JPanel {

private JTextPane lyricsPane;

private LyricsData lyricsData;

private long currentTime;

private Timer scrollTimer;

public LyricsPanel() {

initComponents();

setupLayout();

startScrollTimer();

}

private void initComponents() {

lyricsPane = new JTextPane();

lyricsPane.setEditable(false);

lyricsPane.setFont(new Font(\"Arial\", Font.PLAIN, 14));

// Set up styles

StyleContext sc = StyleContext.getDefaultStyleContext();

AttributeSet normalStyle = sc.addAttribute(SimpleAttributeSet.EMPTY,

StyleConstants.Foreground, Color.BLACK);

AttributeSet highlightStyle = sc.addAttribute(SimpleAttributeSet.EMPTY,

StyleConstants.Foreground, Color.BLUE);

lyricsPane.setCharacterAttributes(normalStyle, true);

}

private void setupLayout() {

setLayout(new BorderLayout());

add(new JScrollPane(lyricsPane), BorderLayout.CENTER);

}

public void setLyrics(LyricsData lyrics) {

this.lyricsData = lyrics;

updateDisplay();

}

public void setCurrentTime(long time) {

this.currentTime = time;

updateDisplay();

}

private void updateDisplay() {

if (lyricsData == null) return;

String currentLyric = lyricsData.getLyricAt(currentTime);

// Update display and highlight current line

// Implement smooth scrolling

}

private void startScrollTimer() {

scrollTimer = new Timer(50, e -\> smoothScroll());

scrollTimer.start();

}

private void smoothScroll() {

// Implement smooth scrolling logic

}

}

// config/PlayerConfig.java

package com.mediaplayer.config;

import java.io.\*;

import java.util.Properties;

public class PlayerConfig {

private static final String CONFIG\_FILE = \"config/player.properties\";

private Properties properties;

private static PlayerConfig instance;

public static PlayerConfig getInstance() {

if (instance == null) {

instance = new PlayerConfig();

}

return instance;

}

private PlayerConfig() {

properties = new Properties();

load();

}

public void load() {

try (InputStream is = new FileInputStream(CONFIG\_FILE)) {

properties.load(is);

} catch (IOException e) {

setDefaults();

}

}

public void save() {

try (OutputStream os = new FileOutputStream(CONFIG\_FILE)) {

properties.store(os, \"Media Player Configuration\");

} catch (IOException e) {

e.printStackTrace();

}

}

private void setDefaults() {

properties.setProperty(\"volume\", \"0.8\");

properties.setProperty(\"repeat\", \"false\");

properties.setProperty(\"shuffle\", \"false\");

properties.setProperty(\"last\_directory\", \"\");

properties.setProperty(\"lyrics\_font\_size\", \"14\");

properties.setProperty(\"theme\", \"system\");

}

public String get(String key) {

return properties.getProperty(key);

}

public void set(String key, String value) {

properties.setProperty(key, value);

}

public float getVolume() {

return Float.parseFloat(get(\"volume\"));

}

public void setVolume(float volume) {

set(\"volume\", String.valueOf(volume));

}

public boolean getRepeat() {

return Boolean.parseBoolean(get(\"repeat\"));

}

public void setRepeat(boolean repeat) {

set(\"repeat\", String.valueOf(repeat));

}

public boolean getShuffle() {

return Boolean.parseBoolean(get(\"shuffle\"));

}

public void setShuffle(boolean shuffle) {

set(\"shuffle\", String.valueOf(shuffle));

}

}

// ui/components/MediaPanel.java

package com.mediaplayer.ui.components;

import javax.swing.\*;

import java.awt.\*;

import com.mediaplayer.model.MediaFile;

import com.mediaplayer.service.MediaPlayerService;

public class MediaPanel extends JPanel {

private MediaPlayerService playerService;

private Component visualComponent;

private JLabel noMediaLabel;

private boolean isFullScreen = false;

private Frame fullScreenWindow;

public MediaPanel(MediaPlayerService playerService) {

this.playerService = playerService;

setLayout(new BorderLayout());

initComponents();

setupListeners();

}

private void initComponents() {

noMediaLabel = new JLabel(\"No media loaded\", SwingConstants.CENTER);

noMediaLabel.setFont(new Font(\"Arial\", Font.BOLD, 18));

add(noMediaLabel, BorderLayout.CENTER);

// Support drag and drop

new FileDrop(this, files -\> {

if (files != null && files.length \> 0) {

try {

MediaFile mediaFile = new MediaFile(files\[0\]);

playerService.loadMedia(mediaFile);

} catch (Exception e) {

showError(\"Failed to load media: \" + e.getMessage());

}

}

});

}

private void setupListeners() {

// Double click for full screen

addMouseListener(new MouseAdapter() {

\@Override

public void mouseClicked(MouseEvent e) {

if (e.getClickCount() == 2) {

toggleFullScreen();

}

}

});

playerService.addListener(new MediaPlayerService.PlayerEventListener() {

\@Override

public void onStateChanged(PlayerState state) {

updateDisplay();

}

\@Override

public void onMediaChanged(MediaFile media) {

handleNewMedia(media);

}

});

}

public void setVisualComponent(Component component) {

if (visualComponent != null) {

remove(visualComponent);

}

if (component != null) {

visualComponent = component;

remove(noMediaLabel);

add(component, BorderLayout.CENTER);

} else {

add(noMediaLabel, BorderLayout.CENTER);

}

revalidate();

repaint();

}

private void handleNewMedia(MediaFile media) {

if (media != null) {

switch (media.getType()) {

case VIDEO:

case IMAGE:

setVisualComponent(playerService.getVisualComponent());

break;

case AUDIO:

showAudioVisualizer();

break;

}

} else {

setVisualComponent(null);

}

}

private void showAudioVisualizer() {

// Implement basic audio visualization

JPanel visualizer = new JPanel() {

\@Override

protected void paintComponent(Graphics g) {

super.paintComponent(g);

drawAudioVisualization(g);

}

};

setVisualComponent(visualizer);

}

private void drawAudioVisualization(Graphics g) {

// Implement audio visualization drawing

}

public void toggleFullScreen() {

if (!isFullScreen) {

fullScreenWindow = new Frame();

fullScreenWindow.setUndecorated(true);

fullScreenWindow.setLayout(new BorderLayout());

if (visualComponent != null) {

remove(visualComponent);

fullScreenWindow.add(visualComponent);

}

GraphicsDevice device = GraphicsEnvironment

.getLocalGraphicsEnvironment()

.getDefaultScreenDevice();

device.setFullScreenWindow(fullScreenWindow);

isFullScreen = true;

// Add escape key listener

fullScreenWindow.addKeyListener(new KeyAdapter() {

\@Override

public void keyPressed(KeyEvent e) {

if (e.getKeyCode() == KeyEvent.VK\_ESCAPE) {

toggleFullScreen();

}

}

});

} else {

GraphicsEnvironment.getLocalGraphicsEnvironment()

.getDefaultScreenDevice()

.setFullScreenWindow(null);

if (visualComponent != null) {

fullScreenWindow.remove(visualComponent);

add(visualComponent);

}

fullScreenWindow.dispose();

fullScreenWindow = null;

isFullScreen = false;

}

}

}

// ui/components/VolumeSlider.java

package com.mediaplayer.ui.components;

import javax.swing.\*;

import java.awt.\*;

import java.awt.event.\*;

public class VolumeSlider extends JSlider {

private Color trackColor = new Color(0, 123, 255);

private Color thumbColor = new Color(0, 98, 204);

private int trackThickness = 4;

private int thumbSize = 12;

public VolumeSlider() {

super(JSlider.HORIZONTAL, 0, 100, 80);

setOpaque(false);

setFocusable(false);

// Custom UI

setUI(new BasicSliderUI(this) {

\@Override

public void paintTrack(Graphics g) {

Graphics2D g2d = (Graphics2D) g;

g2d.setRenderingHint(RenderingHints.KEY\_ANTIALIASING,

RenderingHints.VALUE\_ANTIALIAS\_ON);

int trackY = (trackRect.height - trackThickness) / 2;

// Draw background track

g2d.setColor(Color.LIGHT\_GRAY);

g2d.fillRoundRect(trackRect.x, trackY,

trackRect.width, trackThickness,

trackThickness, trackThickness);

// Draw filled track

int fillWidth = thumbRect.x - trackRect.x;

g2d.setColor(trackColor);

g2d.fillRoundRect(trackRect.x, trackY,

fillWidth, trackThickness,

trackThickness, trackThickness);

}

\@Override

public void paintThumb(Graphics g) {

Graphics2D g2d = (Graphics2D) g;

g2d.setRenderingHint(RenderingHints.KEY\_ANTIALIASING,

RenderingHints.VALUE\_ANTIALIAS\_ON);

g2d.setColor(thumbColor);

int thumbY = thumbRect.y + (thumbRect.height - thumbSize) / 2;

g2d.fillOval(thumbRect.x, thumbY, thumbSize, thumbSize);

}

\@Override

protected Dimension getThumbSize() {

return new Dimension(thumbSize, thumbSize);

}

});

}

}

// Main.java

package com.mediaplayer;

import javax.swing.\*;

import java.awt.\*;

import com.mediaplayer.ui.components.\*;

import com.mediaplayer.service.MediaPlayerService;

import com.mediaplayer.config.PlayerConfig;

public class Main {

public static void main(String\[\] args) {

try {

// Set system look and feel

UIManager.setLookAndFeel(

UIManager.getSystemLookAndFeelClassName());

} catch (Exception e) {

e.printStackTrace();

}

SwingUtilities.invokeLater(() -\> {

try {

createAndShowGUI();

} catch (Exception e) {

showError(\"Failed to start application\", e);

System.exit(1);

}

});

}

private static void createAndShowGUI() {

// Create main frame

JFrame frame = new JFrame(\"Advanced Media Player\");

frame.setDefaultCloseOperation(JFrame.EXIT\_ON\_CLOSE);

// Initialize services

MediaPlayerService playerService = new MediaPlayerService();

PlayerConfig config = PlayerConfig.getInstance();

// Create components

MediaPanel mediaPanel = new MediaPanel(playerService);

PlaylistPanel playlistPanel = new PlaylistPanel();

CustomControlPanel controlPanel = new CustomControlPanel(playerService);

LyricsPanel lyricsPanel = new LyricsPanel();

// Layout

JSplitPane mainSplitPane = new JSplitPane(

JSplitPane.HORIZONTAL\_SPLIT,

mediaPanel,

playlistPanel

);

mainSplitPane.setResizeWeight(0.7);

JSplitPane rightSplitPane = new JSplitPane(

JSplitPane.VERTICAL\_SPLIT,

playlistPanel,

lyricsPanel

);

rightSplitPane.setResizeWeight(0.5);

frame.add(mainSplitPane, BorderLayout.CENTER);

frame.add(controlPanel, BorderLayout.SOUTH);

// Menu bar

JMenuBar menuBar = createMenuBar(frame, playerService);

frame.setJMenuBar(menuBar);

// Set initial size and position

frame.setSize(1024, 768);

frame.setLocationRelativeTo(null);

// Load last state from config

loadInitialState(config, playerService);

frame.setVisible(true);

}

private static JMenuBar createMenuBar(JFrame frame, MediaPlayerService
playerService) {

JMenuBar menuBar = new JMenuBar();

// File menu

JMenu fileMenu = new JMenu(\"File\");

fileMenu.add(new JMenuItem(\"Open File\...\"));

fileMenu.add(new JMenuItem(\"Open Playlist\...\"));

fileMenu.addSeparator();

fileMenu.add(new JMenuItem(\"Exit\"));

// View menu

JMenu viewMenu = new JMenu(\"View\");

viewMenu.add(new JCheckBoxMenuItem(\"Show Playlist\"));

viewMenu.add(new JCheckBoxMenuItem(\"Show Lyrics\"));

// Help menu

JMenu helpMenu = new JMenu(\"Help\");

helpMenu.add(new JMenuItem(\"About\"));

menuBar.add(fileMenu);

menuBar.add(viewMenu);

menuBar.add(helpMenu);

return menuBar;

}

private static void loadInitialState(PlayerConfig config,
MediaPlayerService playerService) {

playerService.setVolume(config.getVolume());

// Load other settings

}

private static void showError(String message, Exception e) {

JOptionPane.showMessageDialog(null,

message + \"\\n\" + e.getMessage(),

\"Error\",

JOptionPane.ERROR\_MESSAGE);

}

}
