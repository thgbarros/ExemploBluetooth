package br.com.barros.newbie.Observers;

/**
 * Created by thiagobarros on 06/04/15.
 */
public interface Observable {

    void attach(Observer observer);
    void deatach(Observer observer);
    void notifyObserver();

}
