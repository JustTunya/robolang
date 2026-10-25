package robolang.theme;

/** Abstract product: a collectible item. */
public interface Collectible extends Paintable {

    /**
     * Returns the message shown when the item is picked up.
     *
     * @return themed pickup text
     */
    String pickupMessage();
}
