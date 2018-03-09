package gibbsmotiffinding;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import sun.security.util.Length;

public class Gibbs {

    private static final String PERSIAN_ALPHABET = "آابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهیئ !#.،؛«»:()1234567890";
    private final float B_J = (float) 0.5;
    private final float B = B_J*PERSIAN_ALPHABET.length();
    
    private List<String> getRandomPositionMotifs(List<String> otherSeq, float[] nonMotifCharCount, int motifLen) {
        List<String> randMotif = new ArrayList<>();
        Random random = new Random();
        int sum = 0;

        for (String s : otherSeq) {
            if (s.length()-motifLen<=0) {
                continue;
            }
            int pos = random.nextInt(s.length() - motifLen);
            if (pos < 0) {
                continue;
            }
            String motif = s.substring(pos, pos + motifLen);
            randMotif.add(motif);

            for (int i = 0; i < pos; i++) {
                if (PERSIAN_ALPHABET.indexOf(s.charAt(i))<0) {
                    continue;
                }
                nonMotifCharCount[PERSIAN_ALPHABET.indexOf(s.charAt(i))]++;
                sum++;
            }

            for (int i = pos + motifLen; i < s.length(); i++) {
                if (PERSIAN_ALPHABET.indexOf(s.charAt(i))<0) {
                    continue;
                }
                nonMotifCharCount[PERSIAN_ALPHABET.indexOf(s.charAt(i))]++;
                sum++;
            }

        }

        for (int i = 0; i < PERSIAN_ALPHABET.length(); i++) {
            nonMotifCharCount[i] = (nonMotifCharCount[i] + B_J) / (sum + B);
            
        }
        return randMotif;
    }

    private float[][] createProfile(List<String> randMotif, int motifLen, int seqCount) {
        float[][] profile = new float[PERSIAN_ALPHABET.length()][motifLen];

        for (String s : randMotif) {
            for (int i = 0; i < s.length(); i++) {
                char ch=s.charAt(i);
                int index=PERSIAN_ALPHABET.indexOf(ch);
                if (index==-1) {
                    continue;
                }
                profile[index][i]++;
            }
        }

        for (int i = 0; i < PERSIAN_ALPHABET.length(); i++) {
            for (int j = 0; j < motifLen; j++) {
                profile[i][j] = (profile[i][j] + B_J) / (seqCount - 1 + B);
            }
        }
        return profile;
    }

    private String getMotifByWeightForChosenSequence(HashMap<Integer, Integer> seqPos, float[][] profile, float[] nonMotifCharCount, String chosenSeq, int chosenSeqIndex, int motifLen) {
        String motif = "";
        for (int i = 0; i < chosenSeq.length() - motifLen; i++) {
            float soorat = 1;
            float makhraj = 1;
            float weight = 0;
            for (int j = i, k=0; j < i + motifLen; j++, k++) {
                char ch=chosenSeq.charAt(j);
                int index=PERSIAN_ALPHABET.indexOf(ch);
                if (index==-1) {
                    continue;
                }
                soorat *= profile[index][k];
                makhraj *= nonMotifCharCount[index];
            }
            float result = soorat / makhraj;
            if (result > weight) {
                weight = result;
                motif = chosenSeq.substring(i, i + motifLen);
                seqPos.put(chosenSeqIndex, i);
            }
        }
        return motif;
    }

    private Set<String> findMotifs(List<String> sequences, int motifLen) {
         Set<String> motifList=new HashSet<>();
        HashMap<Integer, Integer> seqPos = new HashMap<>();
        for (int i = 0; i < sequences.size(); i++) {
            seqPos.put(i, -1);
        }

        for (int i = 0; i < sequences.size(); i++) {
            String chosenSeq = sequences.get(i);
            if (chosenSeq.length() < motifLen) {
                continue;
            }

            List<String> otherSeq = new ArrayList<>(sequences);
            otherSeq.remove(i);

            float[] nonMotifCharCount = new float[PERSIAN_ALPHABET.length()];
            List<String> randMotif = getRandomPositionMotifs(otherSeq, nonMotifCharCount, motifLen);

            float[][] profile = new float[PERSIAN_ALPHABET.length()][motifLen];
            profile = createProfile(randMotif, motifLen, sequences.size());

            String motif = getMotifByWeightForChosenSequence(seqPos, profile, nonMotifCharCount, chosenSeq, i, motifLen);

            motifList.add(motif);
        }

        return motifList;
    }
/*number 1*/ 
    public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("Enter motif length: ");
        Scanner sc = new Scanner(System.in);
        int motifLen = sc.nextInt();

        List<String> sequences = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("d:\\a.txt"))) {
            String line = br.readLine();
            while (line != null) {
                sequences.add(line.trim());
                line = br.readLine();
            }

        }

        Set<String> motifList;
        motifList = new Gibbs().findMotifs(sequences, motifLen);
        for (String s : motifList) {
            System.out.println(s + "\n");
        }
    }
}
