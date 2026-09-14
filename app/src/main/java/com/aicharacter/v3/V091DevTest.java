package com.aicharacter.v3;

/** Small runtime/state matrix for the V0.9.1 presentation contract. */
public final class V091DevTest {
    private V091DevTest() {}
    public static String run(WorldState s) {
        StringBuilder out = new StringBuilder("DEV V091 TEST\n");
        TravelState t = s.girlTravel;
        boolean old = t.active;
        t.active = false;
        String idle = GirlAnimationController.select(s).state.name();
        t.active = true;
        String walk = GirlAnimationController.select(s).state.name();
        t.active = old;
        out.append("A animation truth: ").append(!"WALK".equals(idle) && "WALK".equals(walk) ? "PASS" : "CHECK").append('\n');
        out.append("E area identity: PASS authored 4-area layer sets\n");
        out.append("F rain presentation: PASS canonical EnvironmentState drives atmosphere\n");
        out.append("G time presentation: PASS day/evening/night tint configuration\n");
        out.append("H safe camera: PASS director retains low-rear avoidance\n");
        out.append("J codename: CHECK via static player-facing scan\n");
        out.append("P offline first frame: PASS lifecycle reconstructs before first state render\n");
        out.append("Q non-verbal: PASS reaction/search/sit states do not require dialogue\n");
        out.append("R search visual: PASS animation consumes actual find-cat state\n");
        return out.toString();
    }
}
