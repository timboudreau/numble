package com.mastfrog.numble.acteur;

import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.annotations.HttpCall;
import com.mastfrog.acteur.annotations.Precursors;
import com.mastfrog.acteur.headers.Method;
import static com.mastfrog.acteur.headers.Method.GET;
import com.mastfrog.acteur.preconditions.Methods;
import com.mastfrog.acteur.preconditions.PathRegex;
import com.mastfrog.numble.acteur.FakePage.Foo;
import com.mastfrog.numble.acteur.FakePage.Foo.Bar;
import com.mastfrog.numble.acteur.X.Barble;
import com.mastfrog.numble.acteur.X.Fooble;

/**
 *
 * @author Tim Boudreau
 */
@HttpCall(order = 30000, scopeTypes = {Foo.class, Bar.class})
@PathRegex("^foo\\/bar$")
@Methods({GET, Method.DELETE})
@Precursors({Fooble.class, Barble.class})
public class X extends Acteur {

    static class Fooble extends Acteur {

        Fooble() {
            next(new Foo());
        }
    }

    static class Barble extends Acteur {

    }

    public static class Foo {
        
    }
    public static class Bar {

    }
}
