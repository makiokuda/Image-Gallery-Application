//Modified by Maki Okuda
//Cloud Computing for Business: HW2

package edu.cmu.iccb;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import edu.cmu.iccb.services.ImageService;


@Controller
public class HomeController {
	
    private ImageService imageService;
    
    @Autowired
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

    public ImageService getImageService() {
		return imageService;
	}

    @RequestMapping(method = RequestMethod.GET, value = "/images")
    public String provideUploadInfo(Model model, RedirectAttributes redirectAttributes) {

        List<String> imageIds = imageService.getUploadedImages();        
        model.addAttribute("files", imageIds);     
        return "uploadForm";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/images")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String name = file.getOriginalFilename();

        try {        	
        	imageService.saveImage(name, file.getInputStream());           
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", name + " failed to upload");
        }

        return "redirect:/images";
    }

    //Added for HW2 to provide user authentication via github
    //Reference:https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/java/org/springframework/security/oauth2/client/token/AccessTokenProviderChainTests.java
    @RequestMapping(method = RequestMethod.GET, value = "/login/success")
    public String githubLoginSuccess(RedirectAttributes redirectAttributes, String accessToken) {
        PreAuthenticatedAuthenticationToken auth = new PreAuthenticatedAuthenticationToken
        											("github", accessToken,
        											 Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        return "redirect:/images";
    }
   
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String loginForm(Model model, RedirectAttributes redirectAttributes) {   
        return "login";
    }

    //Added for HW2 to provide user authentication via github
    //Reference: code provided by professor (in above code block) 
    @RequestMapping(method = RequestMethod.GET, value = "/login")
    public String loginForm2(Model model, RedirectAttributes redirectAttributes) {   
        return "redirect:https://github.com/login/oauth/authorize?client_id=bf2eab10400e706f32ac";
    }
}
