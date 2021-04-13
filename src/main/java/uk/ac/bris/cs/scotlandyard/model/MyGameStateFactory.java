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
					throw new IllegalArgumentException("detectives have a secret ticket");
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




		//------------------------------------------------------------------------------------------------------------------------------------
		//helper for available move
		// DETECTIVES SINGLE MOVE

		private static ImmutableSet<Move.SingleMove> makeSingleDetectiveMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source){
			final var singleDetectiveMoves = new ArrayList<Move.SingleMove>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				// TO DO find out if destination is occupied by other detectives
				// if the location is occupied, don't add to the list of moves to return
				//detectives.remove(player);

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
							singleDetectiveMoves.add(new Move.SingleMove(
									player.piece(),
									source,
									t.requiredTicket(),
									destination));
						}
					}
				}
			}

			return ImmutableSet.copyOf(singleDetectiveMoves);
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

		@Nonnull
		@Override public ImmutableSet<Move> getAvailableMoves() {
			Set<Move> merge_moves = new HashSet<>();
			for (Player p : everyone) {
				if (p.isMrX() && p.hasAtLeast(Ticket.DOUBLE, 1) && !setup.rounds.equals(ImmutableList.of(true))) {
					merge_moves = ImmutableSet.<Move>builder()
							.addAll(ImmutableSet.copyOf(makeSingleMoves(setup, detectives, p, p.location())))
							.addAll(ImmutableSet.copyOf(makeDoubleMoves(setup, detectives, p, p.location())))
							.build();
				} else if (p.isMrX()){
					merge_moves = ImmutableSet.copyOf(makeSingleMoves(setup, detectives, p, p.location()));
				} else if (p.isDetective()){
					merge_moves = ImmutableSet.copyOf(makeSingleDetectiveMoves(setup, detectives, p, p.location()));
				}

			}
			moves = ImmutableSet.copyOf(merge_moves);
			return moves;
		}


		@Override public GameState advance(Move move) {
			this.moves = getAvailableMoves();
			if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
			// return gamestate after committing that move
			// check potential problem -> throw illegal
			List<LogEntry> new_log = new ArrayList<>(log);
			LogEntry new_entry = new LogEntry();
			if(move.commencedBy().isMrX() && move.tickets().equals(Ticket.DOUBLE)){
				this.log = new_log.add(move);
			}else if(move.commencedBy().isMrX()){

			}else if(move.commencedBy().isDetective()){

			}


			// need to have a piece to check if move is being done
			// remaining pieces
			// entry list -> for MrX
			// list of all player
			// -add travel log to entry list
			// -check if there is any move -> throw error
			// -make a visit function -> below
			// 		-check for destination -> in that visit, need to add for single move/double move
			// check the original destination
			// list of all destination -> check size, check if player MrX
			// need to extract last element and put in second destination -> loop the destinations
			// make copy of MrX
			// 		for double move -> for 2 destinations
			//		check if get to last ticket
			//		counter to keep ticket
			//		get the last ticket and break the loop
			// check for the round
			// reveal the ticket for MrX
			// 		-> if have secret no reveal
			// reveal last ticket for MrX
			// need to add remaining player back to the 'everyone' list
			// if player is not MrX -> go through the player list to check destination
			//		copy of MrX give it ticket detectives
			// 		loop other player and check if still in game -> add to remaining
			//		add the remaining detective to the list then MrX
			// - make a copy of remaining players and log entry list and return myGameState

			// keep it clean

			return null;


		}
	}
}

