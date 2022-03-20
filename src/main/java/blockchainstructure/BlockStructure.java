package blockchainstructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BlockStructure {

    // This Blockchain Structure program is completely made by Ahmed Hesham Alashi 120191156 ..
    private static String workingDirectory = System.getProperty("user.dir"); // Save The JSON File on the path Of Project 
    private static String jsonFilePath = workingDirectory + "\\src\\main\\java\\blockchainstructure\\blocks.json";
    private static SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchAlgorithmException, JSONException, java.text.ParseException, IOException {
        Blockchain blockchain = new Blockchain();
        blockchain.loadingBlocks();
        // Let's Say Here's The Default Blocks (the 3 line below) :
        // GenesisBlock is added implicilty below ..
        blockchain.addBlock(new Block(1, "Hello", new Date())); // before it's added its index will be checked >> if there's a block added before with that index >> we can't addBlock
        blockchain.addBlock(new Block(2, "Blockchain", new Date()));
        blockchain.addBlock(new Block(3, "Ahmed", new Date()));
        blockchain.addBlock(new Block(4, "Raed Rasheed", new Date()));
        blockchain.exploreBlocks(); // The Output on the console
        blockchain.saveBlocksToJSONFile(); // saving this output into the JSON File ..
    }

    public static class Blockchain {

        private LinkedList<Block> chain = new LinkedList();
        private int difficulty;

        public Blockchain() throws NoSuchAlgorithmException {
            chain.addFirst(GenesisBlock());
            this.difficulty = 2; // 2 zeros
        }

        public Block GenesisBlock() throws NoSuchAlgorithmException {
            return new Block(0, "GenesisBlock", new Date());
        }

        public Block getBlock() {
            return chain.getLast();
        }

        public void addBlock(Block newBlock) throws NoSuchAlgorithmException {
            newBlock.setPreviousHash(this.getBlock().getCurrentHash()); // the last block's hash is the previous block hash of the new-added block :)
            boolean isFound = false;
            for (Block block : chain) {
                if (block.getIndex() == newBlock.getIndex()) {
                    isFound = true;
                    break;
                }
            }
            if (isFound == false) {
                newBlock.mineBlock(this.difficulty);
                chain.addLast(newBlock);
            } else {
                System.err.println("We can't add block with index " + newBlock.getIndex() + " since it's already existed before!\ntry to change the index");
            }
        }

        private void loadingBlocks() throws JSONException, java.text.ParseException, IOException {
            JSONParser jsonParser = new JSONParser();
            File file = new File(jsonFilePath);
            if (Files.lines(Paths.get(jsonFilePath)).count() != 0) {
                try (FileReader reader = new FileReader(jsonFilePath)) {
                    // Read JSON File
                    Object object = jsonParser.parse(reader);
                    JSONArray blocksList = (JSONArray) object;
                    blocksList.forEach(block -> {
                        try {
                            decodeBlockObject((JSONObject) block);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        } catch (java.text.ParseException ex) {
                            ex.printStackTrace();
                        }
                    });
                } catch (FileNotFoundException excpetion) {
                    excpetion.printStackTrace();
                } catch (IOException excpetion) {
                    excpetion.printStackTrace();
                } catch (ParseException excpetion) {
                    excpetion.printStackTrace();
                }
            } else {
                System.err.println("This JSON File is Empty to read!");
            }
        }

        private void decodeBlockObject(JSONObject jsonBlock) throws JSONException, java.text.ParseException {
            // Get Block object within list
            JSONObject blockObject = (JSONObject) jsonBlock.get("block");
            Block tempBlock = new Block();
            tempBlock.setIndex((Long) blockObject.get("id"));
            tempBlock.setData((String) blockObject.get("data"));
            tempBlock.setCurrentHash((String) blockObject.get("current_hash"));
            tempBlock.setPreviousHash((String) blockObject.get("previous_hash"));
            tempBlock.setTimestamp(formatter.parse((String) blockObject.get("timestamp")));
            tempBlock.setNonce((Long) blockObject.get("nonce"));
            boolean isFound = false;
            for (Block block : chain) {
                if ((long) block.getIndex() == tempBlock.getIndex()) {
                    isFound = true;
                    break;
                }
            }
            if (isFound == false) {
                chain.addLast(tempBlock);
            }
        }

        // This method is for saving the blocks in a JSON file ..
        private void saveBlocksToJSONFile() throws JSONException {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < chain.size(); i++) {
                JSONObject object = new JSONObject();
                JSONObject objectItem = new JSONObject();
                objectItem.put("id", chain.get(i).getIndex());
                objectItem.put("data", chain.get(i).getData());
                objectItem.put("timestamp", chain.get(i).getTimestamp().toString());
                objectItem.put("previous_hash", chain.get(i).getPreviousHash());
                objectItem.put("current_hash", chain.get(i).getCurrentHash());
                objectItem.put("nonce", chain.get(i).getNonce());
                object.put("block", objectItem);
                jsonArray.add(object);
            }
            try (FileWriter file = new FileWriter(jsonFilePath)) {
                file.write(jsonArray.toString());
                System.out.println("Succesfully Copied JSON Object (Blocks) to file ..");
                System.out.println("\nJSON Object: " + jsonArray);
            } catch (Exception exception) {
                System.out.println(exception);
            }
        }

        public void exploreBlocks() {
            System.out.println("--------------------------------- :");
            for (Block block : chain) {
                System.out.println("Index : " + block.index
                        + "\nNonce : " + block.nonce
                        + "\nData : " + block.data
                        + "\nTime Stamp : " + block.timestamp
                        + "\nPrevious Hash : " + block.previousHash
                        + "\nHash : " + block.currentHash);
                System.out.println("-----------------------------------------------------------------------");
            }
        }

    }

    public static class Block {

        // Instance Variables: 
        private long index;
        private String data;
        private Date timestamp;
        private String previousHash;
        private String currentHash;
        private long nonce;

        public Block() {
            // No-argument Constructor
        }

        public Block(int index, String data, Date timestamp) throws NoSuchAlgorithmException {
            this.index = index;
            this.data = data;
            this.timestamp = timestamp;
            this.previousHash = "0000000000000000000000000000000000000000000000000000000000000000";
            this.currentHash = calculateHash();
        }

        private static byte[] getSHA(String input) throws NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        }

        private static String toHexString(byte[] hash) {
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        }

        private String calculateHash() throws NoSuchAlgorithmException {
            return toHexString(getSHA(this.index + this.data + this.previousHash + this.timestamp + this.nonce));
        }

        public long getIndex() {
            return index;
        }

        public void setIndex(long index) {
            this.index = index;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public String getPreviousHash() {
            return previousHash;
        }

        public void setPreviousHash(String previousHash) {
            this.previousHash = previousHash;
        }

        public String getCurrentHash() {
            return currentHash;
        }

        public void setCurrentHash(String currentHash) {
            this.currentHash = currentHash;
        }

        public long getNonce() {
            return nonce;
        }

        public void setNonce(long nonce) {
            this.nonce = nonce;
        }

        private void mineBlock(int diffculty) throws NoSuchAlgorithmException {
            String[] array = new String[diffculty + 1];
            for (int i = 0; i < array.length; i++) {
                array[i] = "";
            }
            this.currentHash = this.currentHash.replaceAll(this.currentHash.substring(0, diffculty), String.join("0", array));
            nonce = (int) Math.round(Math.random() * 10);
        }
    }
}
