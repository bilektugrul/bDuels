package io.github.bilektugrul.bduels.users;

import io.github.bilektugrul.bduels.BDuels;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserManager {

    private final BDuels plugin;
    private final Set<User> userList = new HashSet<>();

    public UserManager(BDuels plugin) {
        this.plugin = plugin;
    }

    public User loadUser(Player p) {
        return loadUser(p, true);
    }

    public User loadUser(Player player, boolean keep) {
        User user = new User(player);
        if (keep) userList.add(user);
        return user;
    }

    public User getUser(Player p) {
        UUID uuid = p.getUniqueId();
        return getUser(uuid);
    }

    public User getUser(UUID uuid) {
        for (User user : userList) {
            if (user.getBase().getUniqueId().equals(uuid)) {
                return user;
            }
        }
        return null;
    }

    public Set<User> getUserList() {
        return userList;
    }

}
