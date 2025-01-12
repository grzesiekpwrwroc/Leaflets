package pl.coderslab.leaflets.controller;


import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.leaflets.model.Client;
import pl.coderslab.leaflets.repository.ClientRepository;
import pl.coderslab.leaflets.service.ClientService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.Valid;

@Controller
@RequestMapping("/client")
@Transactional
public class ClientController {

    final ClientRepository clientRepository;
    final ClientService clientService;

    private final String salt = BCrypt.gensalt(); //TODO I don't know it is good idea, but it works

    public ClientController(ClientRepository clientRepository, ClientService clientService) {
        this.clientRepository = clientRepository;
        this.clientService = clientService;
    }

    @GetMapping("/register")
    public String registerClient(Model model) {
        Client client = new Client();
        model.addAttribute(client);
        return "client/registerClient";
    }

    @PostMapping("/register")
    public String registerClient(@Valid Client client, BindingResult result) {
        if(result.hasErrors()){
            return "client/registerClient";
        }
        System.out.println(client);
        //client.setPassword(BCrypt.hashpw(client.getPassword(), BCrypt.gensalt()));
        client.setPassword(BCrypt.hashpw(client.getPassword(), salt));
        clientRepository.save(client);
        //return "/loginClient";
        return "redirect:/client/login";
    }

    @GetMapping("/login")
    public String loginClient(Model model, HttpServletRequest request) {
        model.addAttribute("client", new Client());
        HttpSession session = request.getSession();
        return "client/loginClient";
    }

    @PostMapping("/login")
    public String loginClient(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              HttpSession session) {
        Client client = clientService.findByEmail(email);
        System.out.println(client);
        if (client != null) {
            if (BCrypt.checkpw(password, client.getPassword())) {
                // if (password.equals(client.getPassword())) {
                session.setAttribute("client", client);
            }
            if (session.getAttribute("client") != null) {
                session.setMaxInactiveInterval(300);
                System.out.println("Zalogowano użytkownika o emailu: " + email);
                return "redirect:/client/app/mainPage";
            } else {
                System.out.println("Błędne hasło dla użytkownika o emailu: " + email);
                return "redirect:/client/login";
            }
        } else {
            System.out.println("Nie znaleziono klienta o emailu: " + email);
            return "redirect:/client/login";
        }
    }

    @GetMapping("/app/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/client/login";
    }

    @GetMapping("/app/mainPage")
    public String mainPage() {
        return "client/mainPage";
    }

    @GetMapping("/app/showData")
    public String showData() {
        return "client/showData";
    }

    @GetMapping("/app/editData")
    public String showEditForm(Model model, HttpSession session) {
//        if(session.getAttribute("client")==null){
//            return "redirect:/client/login";
//        }
//        else{
        Client client = (Client) session.getAttribute("client");
        System.out.println("Klient w sesji: " + client);
        model.addAttribute("client", client);
        System.out.println("Metoda get show editForm " + model.getAttribute("client"));
        return "client/editData";
        //}

    }

    @PostMapping("/app/editData")
    public String editData(Client client, HttpSession session) {
        System.out.println("Metoda post show editForm " + client);
        client.setPassword(BCrypt.hashpw(client.getPassword(), salt));
        session.setAttribute("client",client);
        clientService.save(client);

        return "client/showData";
    }
}
