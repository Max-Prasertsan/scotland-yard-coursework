package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import javax.annotation.Nonnull;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
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
				final List<Player> detectives){
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

			// checking part
			// Check if the round is empty.
			if (setup.rounds.isEmpty()) throw new IllegalArgumentException("Round is empty.");

			// Check if Mr X value is empty/null.
			if (mrX == null) throw new NullPointerException("Mr X is empty.");

			// Check that Detectives are not empty.
			if (detectives == null) throw new NullPointerException("Detectives are empty.");

			// Check for 2 Mr X
			for (Player detective : detectives) {
				if (detective.isMrX()) throw new IllegalArgumentException("There's more than 1 Mr X.");
			}

			// Check for swapped Mr X
			if (mrX.isDetective()) throw new IllegalArgumentException("Mr X is invalid");

			// Check for Duplicate detectives
			for (int i = 0; i < detectives.size(); i++){
				for (int j = 0; j < detectives.size(); j++){
					if(i != j){
						if (detectives.get(i).equals(detectives.get(j))) throw new IllegalArgumentException("Duplicate detectives");
					}
				}
			}

			// Check for Location Overlap between Detectives
			for (int i = 0; i < detectives.size(); i++){
				for (int j = 0; j < detectives.size(); j++){
					if (i != j){
						if(detectives.get(i).location()==detectives.get(j).location()) throw new IllegalArgumentException("Location Overlap between Detectives");
					}
				}
			}

			// Check if detectives have secret ticket
			for (int i = 0; i < detectives.size(); i++) {
				if (detectives.get(i).has(Ticket.SECRET))
					throw new IllegalArgumentException("detectives have secret tickets");
			}

			//Check if the detective has a double move ticket
			for (int i = 0; i < detectives.size(); i++) {
				if (detectives.get(i).hasAtLeast(Ticket.DOUBLE,1))
					throw new IllegalArgumentException("detective has a double move ticket");
			}

			//Check if the round is empty
			if(setup.rounds.isEmpty()) throw new IllegalArgumentException("the round is empty");

			//Check if the graph is empty
			if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("the graph is empty");

			//Check if winner is empty at first
			//if(!getWinner().isEmpty()) throw new IllegalArgumentException("Winner should be empty");

		}


		@Override public GameSetup getSetup() {
			return setup;
		}

		@Nonnull
		@Override public ImmutableSet<Piece> getPlayers() {


			List<Piece> all = new ArrayList<>();
			for (Player detective : detectives){
				all.add(detective.piece());
			}

			all.add(mrX.piece());

			remaining = ImmutableSet.copyOf(all);
			return remaining;

		}

		@Nonnull
		@Override public Optional<Integer> getDetectiveLocation(Detective detective) {
			for (Player d : detectives){
				if (d.piece().isDetective()){
					return Optional.of(d.location());
				}
			}
			return Optional.empty();
		}

		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		@Override public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Override public ImmutableSet<Piece> getWinner() {

			Set<Piece> prizeman = new LinkedHashSet<>();
			winner = ImmutableSet.copyOf(prizeman);
			// if (someone won){  // What's the definition of "win" this game...lol
			// prizeman.add(someone)
			// winner = ImmutableSet.copyOf(prizeman);
			// }

			return winner;
		}

		@Override public ImmutableSet<Move> getAvailableMoves() {
			return moves;
		}

		@Override public GameState advance(Move move) {
			return null;
		}
	}
}

