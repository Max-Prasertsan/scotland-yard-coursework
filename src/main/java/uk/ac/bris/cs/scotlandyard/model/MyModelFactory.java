package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.management.modelmbean.ModelMBean;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.units.qual.A;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		return new MyModelState(setup, mrX, detectives);
	}

	private static class MyModelState implements Model{
		private GameSetup setup;
		private Board.GameState gameState;
		private ImmutableList<Player> everyone;

		ArrayList<Observer> observers = new ArrayList<>();
		public MyModelState(GameSetup setup,
							Player mrX,
							ImmutableList<Player> detectives) {
		}


		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return gameState;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if (observer.equals(null)){
				throw new NullPointerException("Observer is empty");
			}
			else if (observers.contains(observer)){
				throw new IllegalArgumentException("Already have this observer");
			}
			else{
				observers.add(observer);
			}
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if (observers.contains(observer)){
				observers.remove(observer);
			}

			else if (observer.equals(null)){
				throw new NullPointerException("This observer is not in the list");
			}
			else{
				throw new IllegalArgumentException("this observer is illegal");
			}
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			gameState.advance(move);
			for (Observer obs : observers){
				obs.onModelChanged(gameState, Observer.Event.MOVE_MADE);
			}

			if(!gameState.getWinner().isEmpty()){
				for (Observer obs : observers){
					obs.onModelChanged(gameState, Observer.Event.GAME_OVER);
				}
			}
		}
	}
}
