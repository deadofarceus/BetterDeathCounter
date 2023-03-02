package betterdeathcounter.model;

import java.util.List;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;
import org.fulib.builder.reflect.Link;

import org.fulib.builder.Type;

public class GenModel implements ClassModelDecorator {

    class Game {
        String name;
        String spreadsheetId;

        @Link("games")
        Player player;

        @Link("game")
        List<Boss> bosses;
    }

    class Player {
        String name;
        String APIUsername;
        double garbageFactor;
        Boolean showLinear;
        Boolean showExp;
        Boolean showTimer;

        @Link
        Game currentGame;

        @Link
        Boss currentBoss;

        @Link("player")
        List<Game> games;
    }

    class Boss {
        String name;
        Boolean secondPhase;

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
