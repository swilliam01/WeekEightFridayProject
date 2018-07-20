package com.example.fido;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    PetRepository petRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String listPets(Model model)
    {
        // See if there is a user logged in, if so pass ID to view
        if(getUser() != null) {
            model.addAttribute("user_id", getUser().getId());
        }
        model.addAttribute("pets", petRepository.findAll());
        return "index";
    }

    @GetMapping("/login")
    public String login()
    {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model)
    {
        model.addAttribute("user", new User());
        return "registration";
    }

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

    @GetMapping("/add")
    public String addPet(Model model)
    {
        model.addAttribute("user_id", getUser().getId());
        model.addAttribute("pet", new Pet());
        return "form";
    }

    @PostMapping("/add")
    public String processForm(@ModelAttribute Pet pet, @RequestParam("file") MultipartFile file, @RequestParam("hiddenImgURL") String ImgURL, @RequestParam("userID") String user_id)
    {
        if(!file.isEmpty())
        {
            try {
                Map uploadResult = cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));
                pet.setImage(uploadResult.get("url").toString());
            } catch (IOException e) {
                e.printStackTrace();
                return "form";
            }
        }
        else {
            if(!ImgURL.isEmpty()) {
                pet.setImage(ImgURL);
            }
            else {
                pet.setImage("");
            }
        }

        User user = userRepository.findById(new Long(user_id)).get();
        pet.setUser(user);
        petRepository.save(pet);
        return "redirect:/";
    }

    @RequestMapping("/update/{id}")
    public String updatePet(@ModelAttribute Pet pet, @PathVariable
            ("id") long id, Model model)
    {
        pet = petRepository.findById(id).get();
        model.addAttribute("user_id", pet.getUser().getId());
        model.addAttribute("pet", petRepository.findById(id));
        model.addAttribute("imageURL", pet.getImage());

        return "form";
    }

    @RequestMapping("/updatestatus/{id}")
    public String updateStatus(@ModelAttribute Pet pet, @PathVariable
            ("id") long id)
    {
        // Find pet associated with ID and update status only
        pet = petRepository.findById(id).get();
        pet.setStatus("Found");
        petRepository.save(pet);
        return "redirect:/";
    }

    @RequestMapping("/getUsers")
    public String showUserList(Model model)
    {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    private User getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentusername = authentication.getName();
        User user = userRepository.findByUsername(currentusername);
        return user;
    }

}