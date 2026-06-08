package com.example.pi.security;

import com.example.pi.entity.Role;
import com.example.pi.entity.User;
import com.example.pi.repository.UserRepository;
import com.example.pi.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email          = oAuth2User.getAttribute("email");
        String firstName      = oAuth2User.getAttribute("given_name");
        String lastName       = oAuth2User.getAttribute("family_name");
        String profilePicture = oAuth2User.getAttribute("picture");

        // Créer ou récupérer l'utilisateur
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setProfileImageUrl(profilePicture);
            newUser.setRole(Role.STUDENT);
            newUser.setPassword("OAUTH2_USER");
            newUser.setActive(true);
            return userRepository.save(newUser);
        });

        // Générer le JWT
        String token = jwtService.generateToken(user.getEmail());

        // Rediriger vers Angular avec le token
        String redirectUrl = "http://localhost:4200/oauth-success?token=" + token
                + "&userId=" + user.getId()
                + "&firstName=" + user.getFirstName()
                + "&lastName=" + user.getLastName()
                + "&email=" + user.getEmail()
                + "&role=" + user.getRole().name();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}