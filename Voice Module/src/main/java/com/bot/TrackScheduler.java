package com.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by Jess on 3/28/2017.
 */
public class TrackScheduler extends AudioEventAdapter{
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private AudioTrack nowPlaying;

    public static final int MAX_QUEUE_SIZE = 20;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
        else {
            nowPlaying = track;
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        nowPlaying = queue.poll();
        player.startTrack(nowPlaying, false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public void pause() {
        player.setPaused(true);
    }

    public void resume() {
        player.setPaused(false);
    }

    public void stopPlayer() {
        player.stopTrack();

        //removes all items from queue before disconnecting
        while (!queue.isEmpty()){
            queue.poll();
        }
        nowPlaying = null;
    }

    public boolean isPlaying() {
        return player.getPlayingTrack() != null;
    }

    public String[] getPlaylist() {
        String[] playlist = new String[queue.size()+1];
        playlist[0] = nowPlaying.getInfo().title;
        int j = 1;
        for (AudioTrack aTrack : queue) {
            playlist[j] = aTrack.getInfo().title;
            j++;
        }
        return playlist;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public String removeTrack(int index) {
        Iterator<AudioTrack> it = queue.iterator();
        AudioTrack track = it.next();
        for (int i = 2; i < index; i++) {
            track = it.next();
        }
        if(track != null){
            it.remove();
            return track.getInfo().title;
        }
        return null;
    }

    public int getNumQueuedTracks() {
        return queue.size();
    }

    public AudioTrack getNowPlaying() {
        return nowPlaying;
    }
}
