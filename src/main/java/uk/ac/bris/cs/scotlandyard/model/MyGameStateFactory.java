package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

	private static final class MyGameState implements GameState {
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



		}


		//--------------------------------------------------------------------------------------------------------------------------------------------
		//helper for available move
		private static ImmutableSet<Move.SingleMove> makeSingleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source){
			final var singleMoves = new ArrayList<Move.SingleMove>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				// TO DO find out if destination is occupied by a detective
				// if the location is occupied, don't add to the list of moves to return
				for (Player d : detectives){
					if (d.location() == destination){
						break ;
					}
				}

				for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))){
					// TO DO find out if the player has the required tickets
					// if it does, construct SingleMove and add it the list of moves to return
					if (player.has(t.requiredTicket())){
						singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
					}
				}
				// TO DO consider the rules of secret moves here
				// add moves to the destination via a secret ticket if there are any left with the player
				if (player.has(Ticket.SECRET)){
					for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))){
						singleMoves.add(new Move.SingleMove(player.piece(), source, Ticket.SECRET, destination));
					}
				}
			}
			return ImmutableSet.copyOf(singleMoves);
		}
		//-----------------------------------------------------------------------------------------------------------------------------------------
		private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source){
			final var doubleMoves = new ArrayList<Move.DoubleMove>();
			Ticket ticket1 = null;
			for (int destination : setup.graph.adjacentNodes(source)) {
				// TO DO find out if destination is occupied by a detective
				// if the location is occupied, don't add to the list of moves to return
				for (Player d : detectives) {
					if (d.location() == destination) {
						break;
					}
				}

				for (Transport t1 : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
					if (player.has(t1.requiredTicket())) {
						ticket1 = t1.requiredTicket();
					}
				}

				if (player.has(Ticket.SECRET)) {
					for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
						ticket1 = Ticket.SECRET;
					}
				}

				for (int destination2 : setup.graph.adjacentNodes(destination)) {
					// TO DO find out if destination is occupied by a detective
					// if the location is occupied, don't add to the list of moves to return
					for (Player d : detectives) {
						if (d.location() == destination2) {
							break;
						}
					}

					for (Transport t2 : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
						if (player.has(t2.requiredTicket())) {
							assert ticket1 != null;
							doubleMoves.add(new Move.DoubleMove(player.piece(), source, ticket1, destination, t2.requiredTicket(), destination2));
						}
					}

					if (player.has(Ticket.SECRET)) {
						for (Transport t2 : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
							assert ticket1 != null;
							doubleMoves.add(new Move.DoubleMove(player.piece(), source, ticket1, destination, Ticket.SECRET, destination2));
						}
					}
				}
			}
			return ImmutableSet.copyOf(doubleMoves);
		}
//-------------------------------------------------------------------------------------------------------------------------------------------------


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
			// For all detectives, if Detective#piece == detective,
			// then return the location in an Optional.of();
			// otherwise, return Optional.empty();
			Optional<Integer> DetectiveAt = Optional.empty();
			//if(!remaining.contains(detective)){ return DetectiveAt; }

			for (Player d : detectives) {
				if (d.piece() == detective) DetectiveAt = Optional.of(d.location());
			}

			return DetectiveAt;
		}


		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			/**
			 * @param piece the player piece
			 * @return the ticket board of the given player; empty if the player is not part of the game
			 */
			//Check that the piece given is a piece in the current game
			for (Player p : everyone){
				if (p.piece() == piece){
					return Optional.of(new TicketBoard() {
						@Override
						public int getCount(@Nonnull Ticket ticket) {
							return p.tickets().get(ticket);
						}
					});
				}
			}
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

			Set<Move> merge_move = new HashSet<>();
			
			for (Player p : everyone){
				if(p.isMrX() && p.has(Ticket.DOUBLE)){
					merge_move = ImmutableSet.<Move>builder()
							.addAll(ImmutableSet.copyOf(makeSingleMoves(setup, detectives, p, p.location())))
							.addAll(ImmutableSet.copyOf(makeDoubleMoves(setup, detectives, p, p.location())))
							.build();
				}else{
					merge_move = ImmutableSet.copyOf(makeSingleMoves(setup, detectives, p, p.location()));
				}
			}
			
			 ImmutableSet<Move> moves = ImmutableSet.copyOf(merge_move);
			
			return  moves;
		}

		@Override public GameState advance(Move move) {
			return null;
		}
	}
}

