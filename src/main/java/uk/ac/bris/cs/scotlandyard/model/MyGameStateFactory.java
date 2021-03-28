package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import java.util.*;
import com.google.common.collect.ImmutableMap;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import javax.annotation.Nonnull;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

	private static final class MyGameState implements GameState {
		private final GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private final Player mrX;
		private final List<Player> detectives;
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

			List<Player> e = new ArrayList<>();
			e.add(mrX);
			e.addAll(detectives);
			everyone = ImmutableList.copyOf(e);

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
						if (detectives.get(i).equals(detectives.get(j))){
							throw new IllegalArgumentException("Duplicate detectives");
						}
					}
				}
			}

			// Check for overlapping location.
			for (int i = 0; i < detectives.size(); i++){
				for (int j = 0; j < detectives.size(); j++){
					if(i != j){
						if (detectives.get(i).location() == detectives.get(j).location()){
							throw new IllegalArgumentException("Duplicate detectives");
						}
					}
				}
			}
			// Check if detective has secret ticket.
			for (Player detective : detectives) {
				if (detective.has(Ticket.SECRET))
					throw new IllegalArgumentException("Detective has secret ticket.");
			}

			// Check if detective has double ticket.
			for (Player detective : detectives) {
				if (detective.hasAtLeast(Ticket.DOUBLE, 1))
					throw new IllegalArgumentException("Detective has double move ticket.");
			}

			// Check for empty graph
			if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("The graph is empty");
		}
/**
		private static ImmutableSet<SingleMove> makeSingleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source){
			final var singleMoves = new ArrayList<SingleMove>();
			for (int destination : setup.graph.adjacentNodes(source)) {
 				for (Transport t : setup.graph.edgeValueOrDefault(source,destination,ImmutableSet.of())){

 				}
			}
 			return ImmutableSet.copyOf(singleMoves);

		}
**/
		@Nonnull
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
			Optional<Integer> DetectiveAt = Optional.empty();
			for (Player d : detectives){
				if (d.piece() == detective) DetectiveAt = Optional.of(d.location());
			}
			return DetectiveAt;
		}




		@Nonnull
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			class makeTicket implements TicketBoard{
				makeTicket(){
					ImmutableMap.of(
							Ticket.TAXI, 0,
							Ticket.BUS, 0,
							Ticket.UNDERGROUND, 0,
							Ticket.DOUBLE, 0,
							Ticket.SECRET, 0);
				}

				public int getCount(@Nonnull Ticket ticket) {
					return 0;
				}
			}
			Optional<ImmutableMap<Ticket, Integer>> lastTick = Optional.empty();
			if (piece.isDetective()){
				lastTick = Optional.of(ScotlandYard.defaultDetectiveTickets());
			}
			else {
				lastTick = Optional.of(ScotlandYard.defaultMrXTickets());
			}
			return lastTick;
		}


		@Nonnull
		@Override public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}

		@Nonnull
		@Override public ImmutableSet<Piece> getWinner() {
			return winner;
		}

		@Nonnull
		@Override public ImmutableSet<Move> getAvailableMoves() {
			return moves;
		}

		@Override public GameState advance(Move move) {
			return null;
		}
	}
}
