package urjc.videotranscoding.core.impl;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import urjc.videotranscoding.core.VideoTranscodingService;
import urjc.videotranscoding.entities.ConversionVideo;
import urjc.videotranscoding.entities.OriginalVideo;
import urjc.videotranscoding.exception.FFmpegException;
import urjc.videotranscoding.service.ConversionVideoService;
import urjc.videotranscoding.service.FileUtils;
import urjc.videotranscoding.wrapper.FfmpegResourceBundle;

@Service
public class VideoTranscodingFFmpegImpl implements VideoTranscodingService {
	private static final Logger logger = Logger.getLogger(VideoTranscodingFFmpegImpl.class);
	private static final String FICH_TRAZAS = "fichero.mensajes.trazas";
	private static final String TRACE_FFMPEG_NULL_OR_EMPTY = "ffmpeg.nullOrEmpty";
	private static final String TRACE_FFMPEG_NOT_FOUND = "ffmpeg.notFound";
	private static final String TRACE_FOLDER_OUTPUT_NULL_OR_EMPTY = "ffmpeg.folderOuput.nullOrEmpty";
	private static final String TRACE_FOLDER_OUPUT_NOT_EXISTS = "ffmpeg.folderOutput.notExits";
	private static final String TRACE_ORIGINAL_VIDEO_NULL = "ffmpeg.originalVideo.null";
	private static final String TRACE_ORIGINAL_VIDEO_NOT_IS_SAVE = "ffmpeg.originalVideo.notSave";
	private final String FFMPEG_INSTALLATION_CENTOS7 = "path.ffmpeg.centos";
	private final String FFMPEG_INSTALLATION_MACOSX = "path.ffmpeg.macosx";
	private StreamGobblerPersistent errorGobbler;
	private StreamGobblerPersistent inputGobbler;
	private StreamGobblerPersistent outputGobbler;
	ExecutorService executorService = Executors.newFixedThreadPool(3);
	@Resource
	private FfmpegResourceBundle ffmpegResourceBundle;
	@Resource
	private Properties propertiesFFmpeg;
	@Resource
	private Properties propertiesFicheroCore;

	@Autowired
	private FileUtils fileUtils;
	@Autowired
	private ConversionVideoService conversionVideoService;
	@Autowired
	private StreamGobblerPersistentFactory streamGobblerPersistentFactory;
	// TODO JAVADOC, LOGGER, EXCEPTS
	// @Autowired
	// private OriginalVideoService originalVideoService;

	@PostConstruct
	public void init() {
		// propertiesFicheroCore = (Properties)
		// ApplicationContextProvider.getApplicationContext()
		// .getBean("propertiesFicheroCore");
		logger.setResourceBundle(ffmpegResourceBundle
				.getFjResourceBundle(propertiesFicheroCore.getProperty(FICH_TRAZAS), Locale.getDefault()));
	}

	@Override
	public String getPathOfProgram() {
		if ((System.getProperty("os.name").equals("Mac OS X"))) {
			return propertiesFFmpeg.getProperty(FFMPEG_INSTALLATION_MACOSX);
		} else {
			return propertiesFFmpeg.getProperty(FFMPEG_INSTALLATION_CENTOS7);
		}
		// TODO otros casos
	}

	/**
	 * 
	 */
	public void transcodeVideo(String pathFFMPEG, String folderOutput, OriginalVideo originalVideo)
			throws FFmpegException {
		if (StringUtils.isBlank(pathFFMPEG)) {
			logger.l7dlog(Level.ERROR, TRACE_FFMPEG_NULL_OR_EMPTY, null);
			throw new FFmpegException(FFmpegException.EX_FFMPEG_EMPTY_OR_NULL);
		}

		if (!fileUtils.exitsFile(pathFFMPEG)) {
			logger.l7dlog(Level.ERROR, TRACE_FFMPEG_NOT_FOUND, new String[] { pathFFMPEG }, null);
			throw new FFmpegException(FFmpegException.EX_FFMPEG_NOT_FOUND, new String[] { pathFFMPEG });

		}
		if (StringUtils.isBlank(folderOutput)) {
			logger.l7dlog(Level.ERROR, TRACE_FOLDER_OUTPUT_NULL_OR_EMPTY, null);
			throw new FFmpegException(FFmpegException.EX_FOLDER_OUTPUT_EMPTY_OR_NULL);
		}

		if (!fileUtils.exitsPath(folderOutput)) {
			logger.l7dlog(Level.ERROR, TRACE_FOLDER_OUPUT_NOT_EXISTS, new String[] { folderOutput }, null);
			throw new FFmpegException(FFmpegException.EX_FOLDER_OUTPUT_NOT_EXITS, new String[] { folderOutput });
		}

		if (originalVideo == null) {
			logger.l7dlog(Level.ERROR, TRACE_ORIGINAL_VIDEO_NULL, null);
			throw new FFmpegException(FFmpegException.EX_ORIGINAL_VIDEO_NULL);
		}
		if (!fileUtils.exitsFile(originalVideo.getPath())) {
			logger.l7dlog(Level.ERROR, TRACE_ORIGINAL_VIDEO_NOT_IS_SAVE, new String[] { originalVideo.getPath() },
					null);
			throw new FFmpegException(FFmpegException.EX_ORIGINAL_VIDEO_NOT_IS_SAVE,
					new String[] { originalVideo.getPath() });
		}
		ExecutorService serviceConversion = Executors.newFixedThreadPool(1);
		serviceConversion.execute(new Runnable() {
			public void run() {
				for (ConversionVideo video : originalVideo.getAllConversions()) {
					if (!video.isActive()) {
						String command = getCommand(pathFFMPEG, new File(originalVideo.getPath()), folderOutput, video);

						try {
							conversionFinal(command, video);
						} catch (FFmpegException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		});
	}

	/**
	 * 
	 * @param command
	 * @param video
	 * @throws FFmpegException
	 */
	private void conversionFinal(String command, ConversionVideo video) throws FFmpegException {
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);
			video.setActive(true);
			conversionVideoService.save(video);
			errorGobbler = streamGobblerPersistentFactory.getStreamGobblerPersistent(proc.getErrorStream(), "ERROR",
					video);
			inputGobbler = streamGobblerPersistentFactory.getStreamGobblerPersistent(proc.getInputStream(), "INPUT",
					video);
			outputGobbler = streamGobblerPersistentFactory.getStreamGobblerPersistent(proc.getInputStream(), "OUTPUT",
					video);
			executorService.execute(errorGobbler);
			executorService.execute(inputGobbler);
			executorService.execute(outputGobbler);
			int exitVal = proc.waitFor();
			if (exitVal == 0) {
				video.setFinished(true);
			} else {
				video.setFinished(false);
			}
		} catch (ExecuteException e) {
			// TODO LOGS
			video.setFinished(false);
			video.setActive(false);
			throw new FFmpegException(e);
		} catch (InterruptedException e) {
			// TODO
			video.setFinished(false);
			video.setActive(false);
			throw new FFmpegException(e);
		} catch (IOException e) {
			// TODO
			video.setFinished(false);
			video.setActive(false);
			throw new FFmpegException(e);
		} finally {
			conversionVideoService.save(video);

		}

	}

	/**
	 * 
	 * @param pathFFMPEG
	 * @param fileInput
	 * @param folderOutput
	 * @param conversionVideo
	 * @return
	 */
	private String getCommand(String pathFFMPEG, File fileInput, String folderOutput, ConversionVideo conversionVideo) {
		String finalPath = folderOutput
				+ getFinalNameFile(fileInput, conversionVideo.getConversionType().getContainerType());
		conversionVideo.setPath(finalPath);
		conversionVideoService.save(conversionVideo);
		String command = pathFFMPEG + " -i " + fileInput.toString()
				+ conversionVideo.getConversionType().getCodecAudioType()
				+ conversionVideo.getConversionType().getCodecVideoType() + finalPath;
		logger.l7dlog(Level.DEBUG, "El Comando que se va a enviar es :" + command, null);
		return command;
	}

	/**
	 * @param fileInput
	 *            of file to converted.
	 * @param extension
	 *            of the futher nameFile
	 * @return String with the final name of the file
	 */
	private String getFinalNameFile(File fileInput, String extension) {
		String sort = String.valueOf(System.currentTimeMillis());
		return "/" + FilenameUtils.getBaseName(fileInput.getName()) + sort.substring(3, 9) + extension;
	}

	/**
	 * 
	 */
	public StreamGobblerPersistent getErrorGobbler() {
		return errorGobbler;
	}

	/**
	 * 
	 */
	public StreamGobblerPersistent getInputGobbler() {
		return inputGobbler;
	}

	/**
	 * 
	 */
	public StreamGobblerPersistent getOutputGobbler() {
		return outputGobbler;
	}

}
