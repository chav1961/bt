package chav1961.bt.mediaplayer.util;

import org.jcodec.codecs.s302.S302MDecoder;
import org.jcodec.common.AudioDecoder;

import chav1961.bt.mediaplayer.filters.MediaInfo;
import chav1961.bt.mediaplayer.pcm.PCMDecoder;

public class PlayerUtils {
    public static AudioDecoder getAudioDecoder(String fourcc, MediaInfo.AudioInfo info) {
        if ("sowt".equals(fourcc) || "in24".equals(fourcc) || "twos".equals(fourcc) || "in32".equals(fourcc))
            return new PCMDecoder(info);
        else if ("s302".equals(fourcc))
            return new S302MDecoder();
        else
            return null;
    }
}
