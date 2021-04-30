package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		Board.GameState gameState = new MyGameStateFactory().build(setup, mrX, detectives);
		return new MyModelState(gameState);
	}

	private static class MyModelState implements Model{
		HashSet<Observer> observers = new HashSet<>();
		Board.GameState gameState;

		// Constructor for model
		public MyModelState(Board.GameState gameState) {
			this.gameState = gameState;
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
			// if not illegal or null, add the observer to the list.
			else{
				observers.add(observer);
			}
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {

			if (observer.equals(null)){
				throw new NullPointerException("This observer is not in the list");
			}
			else if (!observers.contains(observer)){
				throw new IllegalArgumentException("this observer is illegal");
			}
			// if not illegal or null, remove from the list
			else{
				observers.remove(observer);
			}
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {gameState.advance(move);
			gameState = gameState.advance(move);
			if(gameState.getWinner().isEmpty()){
				for (Observer obs : observers){
					obs.onModelChanged(gameState, Observer.Event.MOVE_MADE);
				}
			}
			else{
				for (Observer obs : observers){
					obs.onModelChanged(gameState, Observer.Event.GAME_OVER);
				}
			}
		}
	}
}
