package cz.cvut.fel.berloga.service;

import org.springframework.stereotype.Service;

@Service
public class RegexService {

    // USER REGEX
    protected static final String usernameRegex = "^[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]$";
    protected static final String displayNameRegex = "^[a-zA-Z0-9-_,.ěščřžýáíéúůňďťĚŠČŘŽÝÁÍÉÚŮŇĎŤ]*$";
    protected static final String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    protected static final String passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";
    protected static final String tagRegex = "^[a-z0-9_]$";

    public static boolean checkUsername(String username) {
        return username.matches(RegexService.usernameRegex);
    }

    public static boolean checkDisplayName(String displayName) {
        return displayName.matches(RegexService.displayNameRegex);
    }

    public static boolean checkEmail(String email) {
        return email.matches(RegexService.emailRegex);
    }

    public static boolean checkPassword(String password) {
        return password.matches(RegexService.passwordRegex);
    }

}
