package betterdeathcounter.model;

import java.util.List;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;
import org.fulib.builder.reflect.Link;

import org.fulib.builder.Type;

public class GenModel implements ClassModelDecorator {

    class Player {
        String name;
    
        @Link
        Settings settings;

        @Link
        Game currentGame;
    
        @Link
        Boss currentBoss;
    
        @Link("player")
        List<Game> games;
    }

    class Settings {
        String APIUsername;
        double garbageFactor;
        double cumulativeProbabilityScaling;
        int numBadTrys;
        Boolean showLinear;
        Boolean showExp;
        Boolean showTimer;
        Boolean useCostumPrediction;
    }

    class Game {
        String name;
        String spreadsheetId;

        @Link("games")
        Player player;

        @Link("game")
        List<Boss> bosses;
    }


    class Boss {
        String name;
        Boolean secondPhase;
        double[] prediction;

        @Link("bosses")
        Game game;

        @Link
        List<Death> deaths;
    }

    class Death {
        int percentage;
        int time;
    }

    @Override
    public void decorate(ClassModelManager mm) {
        mm.getClassModel().setDefaultPropertyStyle(Type.BEAN);
		mm.haveNestedClasses(GenModel.class);
    }
}
