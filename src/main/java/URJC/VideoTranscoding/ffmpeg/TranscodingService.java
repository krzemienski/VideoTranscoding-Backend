package URJC.VideoTranscoding.ffmpeg;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import URJC.VideoTranscoding.codecs.ConversionType;
import URJC.VideoTranscoding.exception.FFmpegException;

/**
 * @author luisca
 */
public interface TranscodingService{
	/**
	 * @param pathFFMPEG
	 * @param fileInput
	 * @param folderOutput
	 * @param conversionType
	 * @return
	 * @throws FFmpegException
	 */
	public Map<ConversionType,Boolean> transcode(File pathFFMPEG,File fileInput,Path folderOutput,
				List<ConversionType> conversionType) throws FFmpegException;

	public StreamGobbler getErrorGobbler();

	public StreamGobbler getInputGobbler();

	public StreamGobbler getOutputGobbler();
}
