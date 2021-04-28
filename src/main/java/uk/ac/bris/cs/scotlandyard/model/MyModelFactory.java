package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.management.modelmbean.ModelMBean;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new MyModelState();
	}

	private static class MyModelState implements Model{
		private ArrayList<Observer> observers;
		private GameSetup setup;
		private Board.GameState gameState;

		public static class observer implements Observer {
			@Override
			public void onModelChanged(@Nonnull Board board, @Nonnull Event event) {

			}
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return gameState;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if (observers.contains(observer)){
				observers.remove(observer);
			}
			else{
				throw new IllegalArgumentException("This observer is not in the list");
			}
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			if (!gameState.getWinner().isEmpty()){
				gameState.advance(move);
			}
			else if (gameState.getWinner() == gameState.getPlayers()){

			}
		}
	}
}
