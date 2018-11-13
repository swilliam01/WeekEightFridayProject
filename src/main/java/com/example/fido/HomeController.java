package com.example.fido;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
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
        if(userService.getUser() != null) {
            model.addAttribute("user_id", userService.getUser().getId());
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
        model.addAttribute("pet", new Pet());
        return "form";
    }

    @PostMapping("/add")
    public String processForm(@Valid @ModelAttribute("pet") Pet pet,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              HttpServletRequest request)
    {
        String ImgURL = request.getParameter("hiddenImgURL");
        LocalDate dateLost = LocalDate.parse(request.getParameter("dateLost"));

        if(result.hasErrors()){
            return "form";
        }

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

        pet.setDateLost(dateLost);
        pet.setUser(userService.getUser());
        petRepository.save(pet);
        return "redirect:/";
    }

    @RequestMapping("/update/{id}")
    public String updatePet(@PathVariable ("id") long id, Model model)
    {
        Pet pet = petRepository.findById(id).get();
        model.addAttribute("imageURL", pet.getImage());
        model.addAttribute("pet", pet);

        return "form";
    }

    @RequestMapping("/updatestatus/{id}")
    public String updateStatus(@PathVariable("id") long id)
    {
        // Find pet associated with ID and update status only
        Pet pet = petRepository.findById(id).get();
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



}