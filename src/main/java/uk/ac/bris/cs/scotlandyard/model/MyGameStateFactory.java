package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.units.qual.A;
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
		private final ImmutableList<Player> everyone;
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

			//----------------------------------------------------------------------------------------------------------
			// CHECKING PART
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

			// Check if the winner is empty
			//if (winner.isEmpty()) throw new IllegalArgumentException("The winner is empty");
		}
		//------------------------------------------------------------------------------------------------------------------------------------
		//helper for available move
		// DETECTIVES SINGLE MOVE
		private static ImmutableSet<Move.SingleMove> makeSingleDetectiveMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				Player mrX,
				int source){
			final var singleMoves = new ArrayList<Move.SingleMove>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				// TO DO find out if destination is occupied by a detective
				// if the location is occupied, don't add to the list of moves to return

				detectives.remove(player);

				for (Player d : detectives) {
					if (d.location() == destination && d.location() == mrX.location()){
						break;
					}
					for (Transport t : Objects.requireNonNull(
							setup.graph.edgeValueOrDefault(
									source,
									destination,
									ImmutableSet.of()))) {
						// TO DO find out if the player has the required tickets
						// if it does, construct SingleMove and add it the list of moves to return
						if (d.has(t.requiredTicket())) {
							singleMoves.add(new Move.SingleMove(
									d.piece(),
									source,
									t.requiredTicket(),
									destination));
						}
					}
				}
			}

			return ImmutableSet.copyOf(singleMoves);
		}
		//------------------------------------------------------------------------------------------------------------------------------
		//helper for available move
		// SINGLE MOVE
		private static ImmutableSet<Move.SingleMove> makeSingleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source){
			final var singleMoves = new ArrayList<Move.SingleMove>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				// TO DO find out if destination is occupied by a detective
				// if the location is occupied, don't add to the list of moves to return

				for (Player d : detectives) {
					if (d.location() == destination){
						break;
					}
					for (Transport t : Objects.requireNonNull(
							setup.graph.edgeValueOrDefault(
									source,
									destination,
									ImmutableSet.of()))) {
						// TO DO find out if the player has the required tickets
						// if it does, construct SingleMove and add it the list of moves to return
						if (player.has(t.requiredTicket())) {
							singleMoves.add(new Move.SingleMove(
									player.piece(),
									source,
									t.requiredTicket(),
									destination));
						}
					}
					// TO DO consider the rules of secret moves here
					// add moves to the destination via a secret ticket if there are any left with the player
					if (player.has(Ticket.SECRET)) {
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
			for (int destination1 : setup.graph.adjacentNodes(source)) {
				// TO DO find out if destination is occupied by a detective
				// if the location is occupied, don't add to the list of moves to return
				for (Player d : detectives) {
					if (d.location() == destination1) {
						break;
					}
					for (int destination2 : setup.graph.adjacentNodes(destination1)){
						for (Player d2 : detectives){
							if (d2.location() == destination2){
								break;
							}
							for (Transport t1 : Objects.requireNonNull(
									setup.graph.edgeValueOrDefault(
											source,
											destination1,
											ImmutableSet.of()))) {
								for (Transport t2 : Objects.requireNonNull(
										setup.graph.edgeValueOrDefault(
												destination1,
												destination2,
												ImmutableSet.of()))) {
									if(player.has(t1.requiredTicket()) && player.has(t2.requiredTicket())){
										if (t1.requiredTicket() == t2.requiredTicket()){
											if (player.hasAtLeast(t1.requiredTicket(), 2)){
												doubleMoves.add(new Move.DoubleMove(
														player.piece(),
														source,
														t1.requiredTicket(),
														destination1,
														t2.requiredTicket(),
														destination2));
											}
										}
										else{
											doubleMoves.add(new Move.DoubleMove(
													player.piece(),
													source,
													t1.requiredTicket(),
													destination1,
													t2.requiredTicket(),
													destination2));
										}


										if (player.hasAtLeast(Ticket.SECRET, 1)){
											doubleMoves.add(new Move.DoubleMove(
													player.piece(),
													source,
													Ticket.SECRET,
													destination1,
													t2.requiredTicket(),
													destination2));
											doubleMoves.add(new Move.DoubleMove(
													player.piece(),
													source,
													t1.requiredTicket(),
													destination1,
													Ticket.SECRET,
													destination2));
										}
										if (player.hasAtLeast(Ticket.SECRET, 2)){
											doubleMoves.add(new Move.DoubleMove(
													player.piece(),
													source,
													Ticket.SECRET,
													destination1,
													Ticket.SECRET,
													destination2));
										}
									}
								}
							}
						}
					}
				}
			}
			return ImmutableSet.copyOf(doubleMoves);
		}
//-------------------------------------------------------------------------------------------------------------------------------------------------
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
			for (Player p : everyone){
				if (p.piece() == piece){
					//return Optional.of(ticket -> p.tickets().get(ticket));
					// alternative option using lambda.

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
			Set<Move> merge_moves = new HashSet<>();
			for (Player p : everyone) {
				if (p.isMrX() && p.hasAtLeast(Ticket.DOUBLE, 1) && !setup.rounds.equals(ImmutableList.of(true))) {
					merge_moves = ImmutableSet.<Move>builder()
							.addAll(ImmutableSet.copyOf(makeSingleMoves(setup, detectives, p, p.location())))
							.addAll(ImmutableSet.copyOf(makeDoubleMoves(setup, detectives, p, p.location())))
							.build();
				}
				else if (p.isMrX()){
					merge_moves = ImmutableSet.copyOf(makeSingleMoves(setup, detectives, p, p.location()));
				}
				//if (p.isDetective()){
					//merge_moves = ImmutableSet.copyOf(makeSingleDetectiveMoves(setup, detectives, p, mrX, p.location()));
				//}
			}
			moves = ImmutableSet.copyOf(merge_moves);
			return moves;
		}

		@Override public GameState advance(Move move) {
			if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
			ImmutableList<LogEntry> new_log = null;
			// condition for Mr X
			if (move.commencedBy().isMrX()){
				for (Ticket t : move.tickets()){
					if (t.equals(Ticket.DOUBLE)){
						// the move is a double move
						// need to handle 2 destinations
						// get only the final location, but subtract 2 tickets used.
					}
					else{
						// total used ticket is 1. Just update current position normally.
						// make new ticket list to insert into new MrX.
						// find the final destination of the move, add to current move.visit()
						// need to implement visitor
						Player newMrX = new Player(mrX.piece(), mrX.tickets(), current location);
					}
				}
			}
			// condition for Detectives
			else{
				for (Ticket t : move.tickets()){
					if (t.equals(Ticket.DOUBLE)) throw new IllegalArgumentException("Detective has double move");
					else{
						// subtract the used ticket out of detective tickets list
						// add the ticket to MrX
						// change current location of the detective
						// create new instant of that detective
						// add to the 'remaining' list.
					}
				}

			}
			// use the new updated version of either MrX or detectives
			// change in remaining
			// change in log, add the move that been made by MrX
			return new MyGameState(setup, remaining, log, mrX, detectives);
		}
	}
}
