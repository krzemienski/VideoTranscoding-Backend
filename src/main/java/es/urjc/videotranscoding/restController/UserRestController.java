package es.urjc.videotranscoding.restController;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import es.urjc.videotranscoding.entities.Conversion;
import es.urjc.videotranscoding.entities.Original;
import es.urjc.videotranscoding.entities.User;
import es.urjc.videotranscoding.exception.ExceptionForRest;
import es.urjc.videotranscoding.exception.FFmpegException;
import es.urjc.videotranscoding.service.UserService;
import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/api/user")
@Api(tags = "User Api Operations")
public class UserRestController {
	public interface Details
			extends User.Basic, User.Details, Original.Basic, Original.Details, Conversion.Basic, Conversion.Details {
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

		Optional<User> u = userService.findOneUser(id);

		if (u != null) {
			return new ResponseEntity<>(u.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping(value = "/execute")
	public ResponseEntity<?> executeService() throws FFmpegException {
		userService.callTranscodeIfChargeIsDown();
		return new ResponseEntity<String>("All Ok", HttpStatus.OK);

	}

	/**
	 * Handler for the exceptions
	 * 
	 * @param e
	 *            exception
	 * @return a ExceptionForRest with the exception
	 */
	@ExceptionHandler(FFmpegException.class)
	public ResponseEntity<ExceptionForRest> exceptionHandler(FFmpegException e) {
		ExceptionForRest error = new ExceptionForRest(e.getCodigo(), e.getLocalizedMessage());
		return new ResponseEntity<ExceptionForRest>(error, HttpStatus.BAD_REQUEST);
	}

}
