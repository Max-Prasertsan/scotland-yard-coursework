package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import java.util.*;

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
		private final ImmutableSet<Piece> winner;

		// Constructor
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

			// Number of rounds
			int current_round = log.size();

			//----------------------------------------------------------------------------------------------------------
			// Setting up all player.
			List<Player> e = new ArrayList<>();
			e.add(mrX);
			e.addAll(detectives);
			this.everyone = ImmutableList.copyOf(e);
			//----------------------------------------------------------------------------------------------------------
			// Setting up valid move set.
			Set<Move> moves_mrx = new HashSet<>();
			Set<Move> moves_detective = new HashSet<>();

			for (Piece p : remaining) {
				if (p.isMrX() && mrX.has(Ticket.DOUBLE)
						&& !(setup.rounds.size() == current_round - 1)
						&& !setup.rounds.equals(ImmutableList.of(true))) {
					moves_mrx = ImmutableSet.<Move>builder()
							.addAll(ImmutableSet.copyOf(makeSingleMoves(setup, detectives, mrX, mrX.location())))
							.addAll(ImmutableSet.copyOf(makeDoubleMoves(setup, detectives, mrX, mrX.location())))
							.build();

				} else if (p.isMrX()){
					moves_mrx = ImmutableSet.<Move>builder()
							.addAll(ImmutableSet.copyOf(makeSingleMoves(setup, detectives, mrX, mrX.location())))
							.build();
				} else{
					moves_detective = ImmutableSet.<Move>builder().build();
					for(Player d : detectives){
						if (remaining.contains(d.piece())){
							moves_detective = ImmutableSet.<Move>builder()
									.addAll(moves_detective)
									.addAll(ImmutableSet.copyOf(makeSingleMoves(setup, detectives, d, d.location())))
									.build();
						}
					}
				}
			}

			if (!remaining.contains(mrX.piece())){
				moves = ImmutableSet.copyOf(moves_detective);
			} else{
				moves = ImmutableSet.copyOf(moves_mrx);
			}
			//----------------------------------------------------------------------------------------------------------
			// Setting up winner
			Set<Piece> prizeMan = new LinkedHashSet<>();
			if (!remaining.isEmpty()){
				// when its MrX turn but MrX is out of move
				if(remaining.contains(mrX.piece()) && moves.isEmpty()){
					for (Player d : detectives){
						prizeMan.add(d.piece());
					}
				}
				else if (remaining.contains(mrX.piece())){
					ArrayList<Piece> gone = new ArrayList<>();
					for (Player d : detectives){
						if(!d.has(Ticket.TAXI) && !d.has(Ticket.BUS) && !d.has(Ticket.UNDERGROUND)){
							gone.add(d.piece());
						}

					}
					// when its MrX turn and detectives are all out of tickets
					if (gone.size() == detectives.size()){
						prizeMan.add(mrX.piece());
					}
					// at the end of the game (final round win go to MrX)
					if (setup.rounds.size() == current_round){
						prizeMan.add(mrX.piece());
					}
				}
				// when Detectives are stuck
				else if (moves.isEmpty() && !remaining.contains(mrX.piece())){
					ArrayList<Piece> left = new ArrayList<>();
					for (Player d : detectives){
						if (remaining.contains(d.piece())){
							left.add(d.piece());
						}
					}
					// if all stuck then MrX win
					if (left.size() == 0){
						prizeMan.add(mrX.piece());
					}

				}
				// check if detective land on MrX
				boolean capture = false;
				for (Player d : detectives){
					if (d.location() == mrX.location()) {
						capture = true;
						break;
					}
				}
				// if yes then Detectives capture MrX
				if (capture){
					for (Player d : detectives){
						prizeMan.add(d.piece());
					}
				}
			}
			winner = ImmutableSet.copyOf(prizeMan);

			//----------------------------------------------------------------------------------------------------------
			// CHECKING PART
			// Check if the round is empty.
			if (setup.rounds.isEmpty()) throw new IllegalArgumentException("Round is empty.");

			// Check if Mr X value is empty/null.
			if (mrX == null) throw new NullPointerException("Mr X is empty.");

			// Check that Detectives are not empty/null.
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
						// check for duplicate detectives
						if (detectives.get(i).equals(detectives.get(j))){
							throw new IllegalArgumentException("Duplicate detectives");
						}
						// check for 2 or more detectives in same location
						if (detectives.get(i).location() == detectives.get(j).location()) {
							throw new IllegalArgumentException("Duplicate detectives location");
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
				if (detective.has(Ticket.DOUBLE))
					throw new IllegalArgumentException("Detective has double move ticket.");
			}

			// Check for empty graph
			if (setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("The graph is empty");

		}
		//--------------------------------------------------------------------------------------------------------------
		// helper for checking if location has detective on it.
		// Return false if there is detective on it, true if not.
		private static boolean checkLocation(int location, List<Player> detectives){
			for (Player d : detectives){
				if (d.location() == location){
					return false;
				}
			}
			return true;
		}

		//--------------------------------------------------------------------------------------------------------------
		//helper for available move
		// return Set of SINGLE MOVE
		private static ImmutableSet<Move.SingleMove> makeSingleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source){
			final var singleMoves = new ArrayList<Move.SingleMove>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				if (checkLocation(destination, detectives)){

					for (Transport t : Objects.requireNonNull(
							setup.graph.edgeValueOrDefault(
									source,
									destination,
									ImmutableSet.of()))) {

						if (player.has(t.requiredTicket())) {
							singleMoves.add(new Move.SingleMove(
									player.piece(),
									source,
									t.requiredTicket(),
									destination));
						}
					}
					if (player.has(Ticket.SECRET)) {
						singleMoves.add(new Move.SingleMove(player.piece(), source, Ticket.SECRET, destination));
					}
				}
			}
			return ImmutableSet.copyOf(singleMoves);
		}
		//--------------------------------------------------------------------------------------------------------------
		// helper for MrX double move
		// Return set of DOUBLE MOVE
		private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source){
			final var doubleMoves = new ArrayList<Move.DoubleMove>();

			for (int destination1 : setup.graph.adjacentNodes(source)) {
				if (checkLocation(destination1, detectives)){

					for (int destination2 : setup.graph.adjacentNodes(destination1)){
						if (checkLocation(destination2, detectives)){

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
										} else{
											doubleMoves.add(new Move.DoubleMove(
													player.piece(),
													source,
													t1.requiredTicket(),
													destination1,
													t2.requiredTicket(),
													destination2));
										}


										if (player.has(Ticket.SECRET)){
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
		//--------------------------------------------------------------------------------------------------------------
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
				//alternative option using lambda.
				if (p.piece() == piece){
					return Optional.of(ticket -> p.tickets().get(ticket));

				// Here is normal implementation without lambda.
					//return Optional.of(new TicketBoard() {
						//@Override
						//public int getCount(@Nonnull Ticket ticket) {
							//return p.tickets().get(ticket);
						//}
					// });
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
			if(!winner.isEmpty()){
				Set<Move> empty = new HashSet<>();
				moves = ImmutableSet.copyOf(empty);
			}
			return moves;
		}

		//--------------------------------------------------------------------------------------------------------------
		// Implementing Visitor pattern -> to access destination from Move set
		public static class findMove implements Move.Visitor<Object> {
			@Override
			public Object visit(Move.SingleMove move) {
				return move.destination;
			}

			@Override

			public Object visit(Move.DoubleMove move) {
				return move.destination2;
			}
		}
		//--------------------------------------------------------------------------------------------------------------

		@Nonnull
		@Override public GameState advance(Move move) {
			if (!(moves.contains(move))) throw new IllegalArgumentException("Illegal move: " + move);

			findMove findMoveLocation = new findMove();
			// make copy of MrX
			Player newMrX = mrX;
			// copy of detective
			ArrayList<Player> newDetectives = new ArrayList<>();
			// left is the remaining player in the game
			ArrayList<Piece> left = new ArrayList<>();
			// Copy of log
			ArrayList<LogEntry> newLog = new ArrayList<>(log);

			// condition for Mr X
			if (move.commencedBy().isMrX()){
				for (Ticket t : move.tickets()) {
					if (t.equals(Ticket.DOUBLE)){
						// the move is a double move
						// need to handle 2 destinations
						newMrX = newMrX.use(t);
						break;

					} else {
						// total used ticket is 1.
						// make new ticket list to insert into new MrX.
						// need to implement visitor
						newMrX = newMrX.use(t);

						// reveal at certain round.
						if (setup.rounds.get(newLog.size())){
							newLog.add(LogEntry.reveal(t, (int)move.visit(findMoveLocation)));
						} else{
							newLog.add(LogEntry.hidden(t));
						}
					}

					// update the current MrX position to the destination of the move.
					newMrX = newMrX.at((int)move.visit(findMoveLocation));
				}

				// check if detectives still have tickets, if yes add to remaining
				for (Player d : detectives){
					if (d.has(Ticket.TAXI) || d.has(Ticket.BUS) || d.has(Ticket.UNDERGROUND)){
						left.add(d.piece());
					}
				}
				newDetectives.addAll(detectives);
			}
			// condition for Detectives
			else if (move.commencedBy().isDetective()){
				for (Ticket t : move.tickets()){
					if (t.equals(Ticket.DOUBLE)) throw new IllegalArgumentException("Detective has double move");
					else{
						// subtract the used ticket out of detective tickets list
						// add the ticket to MrX
						// change current location of the detective
						// create new instant of that detective
						for (Player d : detectives){
							if (d.piece().equals(move.commencedBy()) && d.has(t)){
								d = d.use(t);
								newMrX = newMrX.give(t);
								d = d.at((int)move.visit(findMoveLocation));
							}
							// if the detective is not the on making the move
							// check if have ticket then add to the remaining for next round
							else if (remaining.contains(d.piece())
									&& !(d.piece() == move.commencedBy())
									&& (d.has(Ticket.TAXI)
									|| d.has(Ticket.BUS)
									|| d.has(Ticket.UNDERGROUND))){
								left.add(d.piece());
							}
							newDetectives.add(d);
						}
					}
					// if MrX still has ticket, then still in game.
					if ((newMrX.has(Ticket.TAXI)
							|| newMrX.has(Ticket.BUS)
							|| newMrX.has(Ticket.UNDERGROUND))
							&& remaining.size() == 1){
						left.add(newMrX.piece());
					}
				}
			}
			// use the new updated version of either MrX or detectives
			// change in remaining
			// change in log, add the move that been made by MrX
			ImmutableSet<Piece> newRemaining = ImmutableSet.copyOf(left);
			ImmutableList<Player> remainingDetectives = ImmutableList.copyOf(newDetectives);
			ImmutableList<LogEntry> updatedLog = ImmutableList.copyOf(newLog);
			return new MyGameState(setup, newRemaining, updatedLog, newMrX, remainingDetectives);
		}
	}
}
