package URJC.VideoTranscoding.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

	@RequestMapping(value = "/")
	public String getIndex() {
		return "index";
	}

	@RequestMapping(value = "/uploadFile")
	public String getUploadFile() {
		return "uploadFile";
	}

}
