package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import javax.annotation.Nonnull;


/**
 * cw-model
 * Stage 1: Complete this class
 */




public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull
	@Override public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		// TODO
			return new MyGameState (setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);

	}

	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableList<Player> everyone;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

			// For checking parameters.
			if (setup.rounds.isEmpty()) throw new IllegalArgumentException("Round is empty.");
		}


		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Override
		public ImmutableSet<Piece> getPlayers() {
			return remaining;
		}

		@Override
		public Optional<List<Player>> getDetectiveLocation(Piece.Detective detective) {
			// For all detectives, if Detective#piece == detective, then return the location in an Optional.of();
			// otherwise, return Optional.empty();
			if(detective.isDetective()){
				return Optional.of(ImmutableBoard.detectiveLocations.get(detective));
			}else{
				return Optional.empty();
			}
		}

		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Override
		public ImmutableSet<Piece> getWinner() {
			return winner;
		}

		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return moves;
		}

		@Override
		public GameState advance(Move move) {
			return null;
		}
	}
}
