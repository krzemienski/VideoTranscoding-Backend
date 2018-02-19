package urjc.videotranscoding.entities;

import java.io.File;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonView;

import urjc.videotranscoding.codecs.ConversionType;

@Entity
@Table(name = "CONVERSION_VIDEO")
public class ConversionVideo {
	public interface Basic {
	}

	public interface Details {
	}

	/**
	 * Id for the conversionId
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView(Basic.class)
	private long conversionId;
	/**
	 * Name of the video
	 */
	@JsonView(Basic.class)
	private String name;
	/**
	 * Path of the conversion of the video
	 */
	@JsonView(Basic.class)
	private String path;
	/**
	 * Progress of the conversion
	 */
	@JsonView(Details.class)
	private String progress = "0";
	/**
	 * True if videoConversion is finish, false eoc
	 */
	@JsonView(Details.class)
	private boolean finished;
	/**
	 * Active if is in process of conversion, eoc false
	 */
	@JsonView(Details.class)
	private boolean active;
	/**
	 * File Size of the file
	 */
	@JsonView(Details.class)
	private String fileSize;
	/**
	 * Type of conversion for this video
	 */
	@JsonView(Basic.class)
	private ConversionType conversionType;

	/**
	 * Constructor for hibernate
	 */
	protected ConversionVideo() {
	}

	/**
	 * Constructor more simple
	 * 
	 * @param conversion
	 *            for the originalVideo
	 * @param originalVideo
	 *            of the master video
	 */
	public ConversionVideo(ConversionType conversion, OriginalVideo originalVideo) {
		this.name = originalVideo.getName() + "_" + System.currentTimeMillis();
		this.conversionType = conversion;

	}

	/**
	 * Constructor for the diferent name for the video
	 * 
	 * @param name
	 *            for the conversionVideo
	 * @param conversion
	 *            for the video
	 */
	public ConversionVideo(String name, ConversionType conversion) {
		this.name = name;
		this.conversionType = conversion;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public ConversionType getConversionType() {
		return conversionType;
	}

	public void setConversionType(ConversionType conversionType) {
		this.conversionType = conversionType;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFileSize() {
		if (isActive()) {
			return fileSize;
		} else if (isFinished()) {
			File f = new File(getPath());
			return String.valueOf(f.length())+" KB";
		} else
			return "0";
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

}
