package dev.pollywag.nimbusdrop.util;

import dev.pollywag.nimbusdrop.exception.InvalidVerificationTokenException;

import java.time.LocalDateTime;

public class ValidatingVerificationTokenUtil {

    public static void validateVerificationToken(LocalDateTime expirationDate, boolean isTokenUsed) {

        // If the token has expired, throw an exception
        if(expirationDate.isBefore(LocalDateTime.now())){
            throw new InvalidVerificationTokenException("Verification token is expired");
        }

        // If the token was already used, throw an exception
        if(isTokenUsed){
            throw new InvalidVerificationTokenException("Verification token is already used");
        }

    }
}
