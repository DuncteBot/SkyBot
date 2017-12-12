package org.alicebot.ab;


public class AIMLWriter {

   public static String[][] relatives = new String[][]{{"aunt", "her", "who", "aunt"}, {"ant", "her", "who", "aunt"}, {"uncle", "his", "who", "uncle"}, {"friend", "his", "who", "friend"}, {"bestfriend", "his", "who", "bestfriend"}, {"niece", "her", "who", "niece"}, {"nephew", "his", "who", "nephew"}, {"grandmother", "her", "who", "grandmother"}, {"grandma", "her", "who", "grandmother"}, {"grandmom", "her", "who", "grandmother"}, {"mother", "her", "who", "mother"}, {"ma", "her", "who", "mother"}, {"mom", "her", "who", "mother"}, {"momma", "her", "who", "mother"}, {"mum", "her", "who", "mother"}, {"mumma", "her", "who", "mother"}, {"mommy", "her", "who", "mother"}, {"mummy", "her", "who", "mother"}, {"grandfather", "his", "who", "grandfather"}, {"granddad", "his", "who", "grandfather"}, {"father", "his", "who", "father"}, {"dad", "his", "who", "father"}, {"dada", "his", "who", "father"}, {"daddy", "his", "who", "father"}, {"husband", "his", "who", "husband"}, {"hubby", "his", "who", "husband"}, {"wife", "her", "who", "wife"}, {"wifey", "her", "who", "wife"}, {"son", "his", "who", "son"}, {"daughter", "her", "who", "daughter"}, {"brother", "his", "who", "brother"}, {"sister", "her", "who", "sister"}, {"bro", "his", "who", "brother"}, {"sis", "her", "who", "sister"}, {"boyfriend", "his", "who", "boyfriend"}, {"girlfriend", "her", "who", "girlfriend"}};


   public static void familiarContactAIML() {
      for(int i = 0; i < relatives.length; ++i) {
         String familiar = relatives[i][0];
         String pronoun = relatives[i][1];
         String predicate = relatives[i][3];
         String aiml = "<category><pattern>ISFAMILIARNAME " + familiar.toUpperCase() + "</pattern>" + "<template>true</template></category>\n" + "<category><pattern>FAMILIARPREDICATE " + familiar.toUpperCase() + "</pattern>" + "<template>" + predicate + "</template></category>\n" + "<category><pattern>FAMILIARPRONOUN " + familiar.toUpperCase() + "</pattern>" + "<template>" + pronoun + "</template></category>\n";
         System.out.println(aiml);
      }

   }

}
