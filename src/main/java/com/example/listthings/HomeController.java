package com.example.listthings;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    ListingRepository listingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String listMessages(Model model)
    {
        model.addAttribute("listings", listingRepository.findAllByOrderByDate());
        return "index";
    }

    @GetMapping("/login")
    public String login()
    {
        return "login";
    }

    /**
     * for new sign up
     * @param model
     * @return
     */
    @GetMapping("/register")
    public String showRegistrationPage(Model model)
    {
        model.addAttribute("user", new User());
        return "registration";
    }

    /**
     * for new sign up
     * @param user
     * @param result
     * @param model
     * @return
     */
    @PostMapping("/register")
    public String processRegistrationPage(@Valid @ModelAttribute User user, BindingResult result, Model model)
    {
        model.addAttribute("user", user);
        if (result.hasErrors())
        {
            return "registration";
        }
        else
        {
            userService.saveUser(user);
        }
        return "redirect:/";
    }

    /**
     * add new listing
     * @param model
     * @return
     */
    @GetMapping("/add")
    public String addListing(Model model)
    {
        model.addAttribute("user_id", getUser().getId());
        model.addAttribute("listing", new Listing());
        return "form";
    }

    /**
     * add new message
     * @param file
     * @return
     */
    @PostMapping("/add")
    public String processListing(@ModelAttribute Listing listing, @RequestParam("file") MultipartFile file, @RequestParam("hiddenImgURL") String ImgURL, @RequestParam("user_id") String user_id)
    {
        if(!file.isEmpty())
        {
            try {
                Map uploadResult = cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
                listing.setImage(uploadResult.get("url").toString());
            } catch (IOException e) {
                e.printStackTrace();
                return "form";
            }
        }
        else {
            if(!ImgURL.isEmpty()) {
                listing.setImage(ImgURL);
            }
            else {
                listing.setImage("");
            }
        }

        listing.setUser(userRepository.findById(new Long(user_id)).get());
        listing.setDate();
        listingRepository.save(listing);
        return "redirect:/";
    }

    /**
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/update/{id}")
    public String updateListing(@ModelAttribute Listing listing, @PathVariable
            ("id") long id, Model model)
    {
        listing = listingRepository.findById(id).get();
        model.addAttribute("user_id", listing.getUser().getId());
        model.addAttribute("listing", listingRepository.findById(id));
        model.addAttribute("imageURL", listing.getImage());

        if(listing.getImage().isEmpty()) {
            model.addAttribute("imageLabel", "Upload Image");
        }
        else {
            model.addAttribute("imageLabel", "Upload New Image");
        }

        return "form";
    }

    /**
     * delete message by only message owner and admin
     * @param id
     * @return
     */
    @RequestMapping("/delete/{id}")
    public String deleteListing(@PathVariable("id") long id)
    {
        listingRepository.deleteById(id);
        return "redirect:/";
    }

    /**
     * display all of the users
     * @param model
     * @return go to list of users page
     */
    @RequestMapping("/showuserprofile")
    public String showUserList(Model model)
    {
        model.addAttribute("users", userRepository.findAll());
        return "userprofile";
    }

    private User getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentusername = authentication.getName();
        User user = userRepository.findByUsername(currentusername);
        return user;
    }

}