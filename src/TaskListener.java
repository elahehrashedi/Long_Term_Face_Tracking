/**
 * Created by Elaheh on 5/4/2016.
 */
public interface TaskListener {
    /**

     * Notifies this object that the Runnable object has completed its work.

     * @param runner The runnable interface whose work has finished.

     */

    public void threadComplete( Runnable runner );
}
