package dev.pollywag.nimbusdrop.util;

import dev.pollywag.nimbusdrop.exception.InvalidVerificationTokenException;

import java.time.LocalDateTime;

public class ValidatingVerificationTokenUtil {

    public static void validateVerificationToken(LocalDateTime expirationDate, boolean isTokenUsed) {

        if(expirationDate.isBefore(LocalDateTime.now())){
            throw new InvalidVerificationTokenException("Verification token is expired");
        }

        if(isTokenUsed){
            throw new InvalidVerificationTokenException("Verification token is already used");
        }

    }
}
