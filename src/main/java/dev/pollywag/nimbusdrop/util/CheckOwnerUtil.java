package dev.pollywag.nimbusdrop.util;

import dev.pollywag.nimbusdrop.entity.Drop;
import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.User;

public class CheckOwnerUtil {

    public static boolean checkNimbusOwnerValidity(Nimbus nimbus, User user) {
        Long userId = user.getId();
        Long nimbusOwnerId = nimbus.getUser().getId();

        return !userId.equals(nimbusOwnerId);
    }

    public static boolean checkDropOwnerValidity(Drop drop, User user) {
        Long userId = user.getId();
        Long drowOwnerId = drop.getNimbus().getUser().getId();

        return !userId.equals(drowOwnerId);
    }










}
