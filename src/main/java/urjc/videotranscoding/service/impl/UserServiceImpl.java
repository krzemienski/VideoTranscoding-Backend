package urjc.videotranscoding.service.impl;

import java.security.Principal;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import urjc.videotranscoding.core.VideoTranscodingService;
import urjc.videotranscoding.entities.OriginalVideo;
import urjc.videotranscoding.entities.User;
import urjc.videotranscoding.exception.FFmpegException;
import urjc.videotranscoding.repository.UserRepository;
import urjc.videotranscoding.service.UserService;

@Service
@SuppressWarnings("unused")
public class UserServiceImpl implements UserService {
	// TODO SUPRESSWARNINGS
	private final String DEFAULT_UPLOAD_FILES = "path.folder.ouput";
	private final String FFMPEG_INSTALLATION_CENTOS7 = "path.ffmpeg.centos";
	private final String FFMPEG_INSTALLATION_MACOSX = "path.ffmpeg.macosx";
	@Autowired
	private FileServiceImpl fileService;
	@Resource
	private Properties propertiesFFmpeg;
	@Autowired
	private VideoTranscodingService transcode;
	@Autowired
	private UserRepository users;

	public User findByEmail(String email) {
		return users.findByEmail(email);
	}

	public List<User> findAllUsers() {
		return users.findAll();
	}

	public Page<User> findAllUsersPage(Pageable page) {
		return users.findAll(page);
	}

	public User findOneUser(long id) {
		return users.findOne(id);
	}

	public User findOneUser(String nombreUsuario) {
		return users.findByNick(nombreUsuario);
	}

	public void deleteUsers(long id) {
		users.delete(id);
	}

	public boolean exists(long id) {
		return users.exists(id);
	}

	public void isAdminVisitorLogged(Principal principal, Model m) {
		boolean isLogged = principal != null;
		User visitor = (isLogged) ? users.findByEmail(principal.getName()) : null;
		m.addAttribute("visitor", visitor);
		m.addAttribute("isLogged", isLogged);
		m.addAttribute("isAdmin", (visitor != null && visitor.isAdmin()));
	}

	public User save(User u) {
		return users.save(u);
	}

	public User userVisitor(Principal principal) {
		User user = users.findByEmail(principal.getName());
		return user;
	}

	public boolean isLogged(Principal principal) {
		if (principal == null)
			return false;
		return !(users.findByEmail(principal.getName()) == null);
	}

	public boolean isAdmin(Principal principal) {
		if (principal == null)
			return false;
		User u = users.findByEmail(principal.getName());
		if (u == null)
			return false;
		return u.isAdmin();
	}

	// @Scheduled(cron = "*/10 * * * * *")
	private void callTranscodeIfChargeIsDown() {
		users.findAll().forEach(x -> {
			for (OriginalVideo originalVideo : x.getListVideos()) {
				if (!originalVideo.isActive()) {
					String FFMPEG_PATH;
					if ((System.getProperty("os.name").equals("Mac OS X"))) {
						FFMPEG_PATH = propertiesFFmpeg.getProperty(FFMPEG_INSTALLATION_MACOSX);
					} else {
						FFMPEG_PATH = propertiesFFmpeg.getProperty(FFMPEG_INSTALLATION_CENTOS7);
					}
					// Path pathToReturn = fileService.saveFile(file,
					// propertiesFFmpeg.getProperty(DEFAULT_UPLOAD_FILES));
					try {
						transcode.transcodeVideo(FFMPEG_PATH, "/Users/luisca/Documents/VideosPrueba", originalVideo);
					} catch (FFmpegException e) {
						// TODO LANZA EXCEPCION GENERICA POR FALLO DEL SISTEMA
						e.printStackTrace();
					}

				}
			}

		});

	}
}