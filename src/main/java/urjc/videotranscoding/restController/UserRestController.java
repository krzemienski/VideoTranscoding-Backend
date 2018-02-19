package urjc.videotranscoding.restController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import urjc.videotranscoding.entities.ConversionVideo;
import urjc.videotranscoding.entities.OriginalVideo;
import urjc.videotranscoding.entities.User;
import urjc.videotranscoding.exception.FFmpegException;
import urjc.videotranscoding.service.UserService;

@RestController
@RequestMapping(value = "/api/user")
public class UserRestController {
	public interface Details extends User.Basic, User.Details, OriginalVideo.Basic, OriginalVideo.Details,
			ConversionVideo.Basic, ConversionVideo.Details {
	}

	@Autowired
	private UserService userService;

	@JsonView(User.Basic.class)
	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<List<User>> getUsers() {
		List<User> users = userService.findAllUsers();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@JsonView(Details.class)
	@GetMapping(value = "/{id}")
	public ResponseEntity<User> getSingleUser(@PathVariable long id) {

		User u = userService.findOneUser(id);

		if (u != null) {
			return new ResponseEntity<>(u, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping(value = "/execute")
	public String executeService() {
		try {
			userService.callTranscodeIfChargeIsDown();
			return "ok";
		} catch (FFmpegException e) {
			return e.getMessage();
		}
	}

}
