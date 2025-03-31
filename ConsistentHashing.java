import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashing {
    private final NavigableMap<Long, String> ring = new TreeMap<>();
    private final int virtualNodes;


    public ConsistentHashing(int virtualNodes) {
        this.virtualNodes = virtualNodes;
    }

    // Add a server with multiple virtual nodes
    public void addServer(String server) {
        for (int i = 0; i < virtualNodes; i++) {
            long hash = hash(server + "#" + i);
            ring.put(hash, server);
        }
    }

    // Remove a server and its virtual nodes
    public void removeServer(String server) {
        for (int i = 0; i < virtualNodes; i++) {
            long hash = hash(server + "#" + i);
            ring.remove(hash);
        }
    }

    // Get the server responsible for a given key
    public String getServer(String key) {
        if (ring.isEmpty()) {
            return null;
        }
        long hash = hash(key);
        SortedMap<Long, String> tailMap = ring.tailMap(hash);
        long targetHash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        return ring.get(targetHash);
    }

    // Hash function using MD5
    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return ((long) (digest[3] & 0xFF) << 24) | ((long) (digest[2] & 0xFF) << 16)
                    | ((long) (digest[1] & 0xFF) << 8) | (digest[0] & 0xFF);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public static void main(String[] args) {
        ConsistentHashing hashRing = new ConsistentHashing(3);

        // Add servers
        hashRing.addServer("Server1");
        hashRing.addServer("Server2");
        hashRing.addServer("Server3");

        // Test key distribution
        String[] keys = {"User1", "User2", "User3", "User4", "User5"};
        for (String key : keys) {
            System.out.println("Key " + key + " is mapped to " + hashRing.getServer(key));
        }

        // Remove a server and recheck distribution
        System.out.println("\nRemoving Server2...");
        hashRing.removeServer("Server2");

        for (String key : keys) {
            System.out.println("Key " + key + " is now mapped to " + hashRing.getServer(key));
        }
    }
}

