package fifteen;

import java.util.*;

class point {
	char col;
	int row;

	int on_board = 0;// 0이면 안 놓은 곳. 1이면 흑, -1이면 백.
	int black_weight = 0, white_weight = 0, value;// value는
													// black_weight+white_weight
	char icon; // 화면에 표시되는 상태 char.

	public point(int col, int row, char icon) {
		this.col = (char) col;
		this.row = row;
		this.icon = icon;
	}

}

class loc {
	int row, col, score = 0; // score: 다음 둘 후보를 정하는 데 이용하는 점수.

	public loc(int row, int col) {
		this.row = row;
		this.col = col;
	}
}

class omok15 {
	point[][] board;
	int count = 0; // turn count.
	Scanner keyboard;

	int leftbound = 0, rightbound = 14; // 오목판의 가장 왼쪽에 위치한 흑돌과 가장 오른쪽에 위치한 흑돌의
										// col
	int upbound = 0, downbound = 14; // 오목판의 가장 위쪽에 위치한 흑돌과 가장 아래쪽에 위치한 흑돌의
										// row

	int alpha, beta; // variables used for alpha-beta pruning.
	int a = 0, b = 0, max_a = -1, max_b = -1, max = -1;

	public omok15() { // omok15 클래스 생성자. 보드 metadata 초기화.

		keyboard = new Scanner(System.in);
		board = new point[15][15];

		// 첫 줄
		board[0][0] = new point(65, 15, '┌');
		for (int i = 1; i < 14; i++)
			board[0][i] = new point(65 + i, 5, '┬');
		board[0][14] = new point(79, 15, '┐');

		// 중간 13줄
		for (int i = 1; i <= 13; i++) {
			for (int j = 0; j < 15; j++) {
				if (j == 0) {
					board[i][j] = new point(65 + j, 15 - i, '├');
				} else if (j == 14) {
					board[i][j] = new point(65 + j, 15 - i, '┤');
				} else {
					board[i][j] = new point(65 + j, 15 - i, '┼');
				}
			}
		}

		// 마지막 줄
		board[14][0] = new point(65, 1, '└');
		for (int i = 1; i < 14; i++)
			board[0][i] = new point(65 + i, 1, '┴');
		board[14][14] = new point(79, 1, '┘');

		// 가중치 초기화 (black_weight, white_Weight)
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				// 가로 체크
				if (j <= 10) {
					board[i][j].black_weight++;
					board[i][j].white_weight++;

					board[i][j + 1].black_weight++;
					board[i][j + 1].white_weight++;

					board[i][j + 2].black_weight++;
					board[i][j + 2].white_weight++;

					board[i][j + 3].black_weight++;
					board[i][j + 3].white_weight++;

					board[i][j + 4].black_weight++;
					board[i][j + 4].white_weight++;
				}
				// 세로 체크
				if (i <= 10) {
					board[i][j].black_weight++;
					board[i][j].white_weight++;

					board[i + 1][j].black_weight++;
					board[i + 1][j].white_weight++;

					board[i + 2][j].black_weight++;
					board[i + 2][j].white_weight++;

					board[i + 3][j].black_weight++;
					board[i + 3][j].white_weight++;

					board[i + 4][j].black_weight++;
					board[i + 4][j].white_weight++;
				}
				// 우하향 대각선 체크
				if (i <= 10 && j <= 10) {
					board[i][j].black_weight++;
					board[i][j].white_weight++;

					board[i + 1][j + 1].black_weight++;
					board[i + 1][j + 1].white_weight++;

					board[i + 2][j + 2].black_weight++;
					board[i + 2][j + 2].white_weight++;

					board[i + 3][j + 3].black_weight++;
					board[i + 3][j + 3].white_weight++;

					board[i + 4][j + 4].black_weight++;
					board[i + 4][j + 4].white_weight++;
				}
				// 좌하향 대각선 체크
				if (j >= 4 && i <= 10) {
					board[i][j].black_weight++;
					board[i][j].white_weight++;

					board[i + 1][j - 1].black_weight++;
					board[i + 1][j - 1].white_weight++;

					board[i + 2][j - 2].black_weight++;
					board[i + 2][j - 2].white_weight++;

					board[i + 3][j - 3].black_weight++;
					board[i + 3][j - 3].white_weight++;

					board[i + 4][j - 4].black_weight++;
					board[i + 4][j - 4].white_weight++;
				}
			}
		}

		// value 초기화 (black_weight+white_weight)
		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++)
				board[i][j].value = board[i][j].black_weight
						+ board[i][j].white_weight;
	}

	private void victory_endgame(int team) {
		if (team == 1) { // 흑색 승리
			System.out.println("\n흑 선수(A.I.)의 승리입니다. GAME OVER.");
			System.exit(0);
		} else if (team == -1) { // 백색 승리
			System.out.println("\n백 선수(A.I.)의 승리입니다. VICTORY.");
			System.exit(0);
		}
	}

	private void check_row_victory(int team) { // 행 승리조건 검사
		int check = 0;
		for (int i = 0, j; i <= 14; i++) {
			for (j = 0; j <= 14; j++) {
				if (board[i][j].on_board == team)
					check++;
			}
			if (check == 5)
				victory_endgame(team);
			else
				check = 0;
		}
	} // end : check_row_victory

	private void check_col_victory(int team) { // 열 승리조건 검사
		int check = 0;

		for (int i = 0, j; i <= 14; i++) {
			for (j = 0; j <= 14; j++) {
				if (board[j][i].on_board == team)
					check++;
			}
			if (check == 5)
				victory_endgame(team);
			else
				check = 0;
		}
	} // end : check_col_victory

	private void check_diag_victory(int team) { // 대각선 승리조건 검사

		// 우하향 대각선 승리조건 검사
		for (int i = 0; i <= 10; i++) {
			for (int j = 0; j <= 10; j++) {
				if (board[i][j].on_board + board[i + 1][j + 1].on_board
						+ board[i + 2][j + 2].on_board
						+ board[i + 3][j + 3].on_board
						+ board[i + 4][j + 4].on_board == 5)
					victory_endgame(team);
			}
		}
		// 좌하향 대각선 승리조건 검사
		for (int i = 0; i <= 10; i++) {
			for (int j = 4; j <= 14; j++) {
				if (board[i][j].on_board + board[i + 1][j - 1].on_board
						+ board[i + 2][j - 2].on_board
						+ board[i + 3][j - 3].on_board
						+ board[i + 4][j - 4].on_board == 5)
					victory_endgame(team);
			}
		}
	} // end : check_diag_victory

	public void printboardstat() { // 오목판의 현 상태를 출력합니다.
		System.out.println("   A B C D E F G H I J K L M N O");

		for (int i = 0; i < 5; i++) {
			System.out.print((15 - i) + " "); // 행 번호 출력 (10~15)
			for (int j = 0; j < 14; j++)
				System.out.print(board[i][j].icon + "─");
			System.out.println(board[i][14].icon);
		}

		for (int i = 5; i < 15; i++) {
			System.out.print(" " + (15 - i) + " "); // 행 번호 출력 (1~9). 자릿수 공백상쇄.
			for (int j = 0; j < 14; j++)
				System.out.print(board[i][j].icon + "─");
			System.out.println(board[i][14].icon);
		}

	}

	public void hansoo_black() { // recursive 함수를 호출하여 다음에 둘 수를 결정한다.
		int foresee = 7; // foresee만큼 앞을 내다본다.
		int finalist_row, finalist_col, max_score;
		LinkedList candidates = new LinkedList(); // 다음 탐색할 후보 위치들 저장.

		// recursive(foresee);
		// chaksoo_black(finalist_row, finalist_col);
	}

	public void hansoo_white() {
		while (true) {
			System.out.println("백 돌을 둘 좌표를 선택해주세요.");
			String white_input = keyboard.nextLine();
			char col_c = white_input.toLowerCase().charAt(0);
			int col;
			if (col_c == 'a')
				col = 0;
			else if (col_c == 'b')
				col = 1;
			else if (col_c == 'c')
				col = 2;
			else if (col_c == 'd')
				col = 3;
			else if (col_c == 'e')
				col = 4;
			else if (col_c == 'f')
				col = 5;
			else if (col_c == 'g')
				col = 6;
			else if (col_c == 'h')
				col = 7;
			else if (col_c == 'i')
				col = 8;
			else if (col_c == 'j')
				col = 9;
			else if (col_c == 'j')
				col = 10;
			else if (col_c == 'j')
				col = 11;
			else if (col_c == 'j')
				col = 12;
			else if (col_c == 'j')
				col = 13;
			else if (col_c == 'j')
				col = 14;
			else { // 유효하지 않은 열 입력시 재요청.
				System.out.println("Wrong input");
				System.out.println("enter valid input.Try again.");
				continue;
			}
			int row = Character.getNumericValue(white_input.charAt(1));
			if (row > 15 || row < 1) // 유효하지 않은 행 입력시 재요청.
			{
				System.out.println("enter valid input.Try again.");
				continue;
			}
			// 해당 위치에 돌이 있는지 검사
			if (board[15 - row][col].on_board != 0) {
				System.out
						.println("a stone is already in the position.\nTry again.");
				continue;
			}

			// 33 조건 검사 (사용자 입력)
			if (check33(15 - row, col, -1)) {
				System.out.println("33 위치는 둘 수 없습니다. 좌표를 재입력해주세요\n");
				continue; // 33이 발생하는 위치이므로 두지 않는다.
			}

			// 입력된 위치에 한 수 두기 (백 선수)
			chaksoo_white(15 - row, col);
			break;
		}
	}

	private boolean check33(int i, int j, int team) {
		int count33 = 0; // 빈 3 개수
		int a, b, x, y;
		board[i][j].on_board = team; // temporarily

		// 가로 빈 3 count
		a = i;
		x = Math.max(0, j - 4);
		y = Math.min(9, j - 1);
		for (b = x; b <= y; b++) {
			// 가로 붙은 빈 3 체크 ○●●●○
			if (board[a][b].on_board == 0 && board[a][b + 1].on_board == team
					&& board[a][b + 2].on_board == team
					&& board[a][b + 3].on_board == team
					&& board[a][b + 4].on_board == 0) {
				count33 += 1;
				break;
			}
			// 가로 빈 3 체크 ○●●○●○ + 가로 빈 3 체크 ○●○●●○
			if (b <= 8) {
				// 체크 ○●●○●○
				if (board[a][b].on_board == 0
						&& board[a][b + 1].on_board == team
						&& board[a][b + 2].on_board == team
						&& board[a][b + 3].on_board == 0
						&& board[a][b + 4].on_board == team
						&& board[a][b + 5].on_board == 0) {
					count33 += 1;
					break;
				}
				// 체크 ○●○●●○
				if (board[a][b].on_board == 0
						&& board[a][b + 1].on_board == team
						&& board[a][b + 2].on_board == 0
						&& board[a][b + 3].on_board == team
						&& board[a][b + 4].on_board == team
						&& board[a][b + 5].on_board == 0) {
					count33 += 1;
					break;
				}
			}
		}

		// 세로 33 체크
		b = j;
		x = Math.max(0, i - 4);
		y = Math.min(9, i - 1);
		for (a = x; a <= y; a++) {
			// 세로 붙은 빈 3 체크 ○●●●○
			if (board[a][b].on_board == 0 && board[a + 1][b].on_board == team
					&& board[a + 2][b].on_board == team
					&& board[a + 3][b].on_board == team
					&& board[a + 4][b].on_board == 0) {
				count33 += 1;
				break;
			}
			// 세로 빈 3 체크 ○●●○●○ + 세로 빈 3 체크 ○●○●●○
			if (a <= 8) {
				// 체크 ○●●○●○
				if (board[a][b].on_board == 0
						&& board[a + 1][b].on_board == team
						&& board[a + 2][b].on_board == team
						&& board[a + 3][b].on_board == 0
						&& board[a + 4][b].on_board == team
						&& board[a + 5][b].on_board == 0) {
					count33 += 1;
					break;
				}
				// 체크 ○●○●●○
				if (board[a][b].on_board == 0
						&& board[a + 1][b].on_board == team
						&& board[a + 2][b].on_board == 0
						&& board[a + 3][b].on_board == team
						&& board[a + 4][b].on_board == team
						&& board[a + 5][b].on_board == 0) {
					count33 += 1;
					break;
				}
			}
		}

		// 우하향 33 체크
		x = Math.min(9, i - 1);
		y = Math.min(9, j - 1);
		for (a = i - 4, b = j - 4; a <= x && b <= y; a++, b++) {
			if (a < 0 || b < 0)
				continue;
			// 우하향 ○●●●○ 체크
			if (board[a][b].on_board == 0
					&& board[a + 1][b + 1].on_board == team
					&& board[a + 2][b + 2].on_board == team
					&& board[a + 3][b + 3].on_board == team
					&& board[a + 4][b + 4].on_board == 0) {
				count33 += 1;
				break;
			}

			// 우하향 빈 3 체크 ○●●○●○ + 우하향 빈 3 체크 ○●○●●○
			if (a <= 8 && b <= 8) {
				// 우하향 빈 3 체크 ○●●○●○
				if (board[a][b].on_board == 0
						&& board[a + 1][b + 1].on_board == team
						&& board[a + 2][b + 2].on_board == team
						&& board[a + 3][b + 3].on_board == 0
						&& board[a + 4][b + 4].on_board == team
						&& board[a + 5][b + 5].on_board == 0) {
					count33 += 1;
					break;
				}
				// 우하향 빈 3 체크 ○●○●●○
				if (board[a][b].on_board == 0
						&& board[a + 1][b + 1].on_board == team
						&& board[a + 2][b + 2].on_board == 0
						&& board[a + 3][b + 3].on_board == team
						&& board[a + 4][b + 4].on_board == team
						&& board[a + 5][b + 5].on_board == 0) {
					count33 += 1;
					break;
				}
			}
		}

		// 좌하향 33 체크
		x = Math.min(9, i - 1);
		y = Math.max(5, j + 1);
		for (a = i - 4, b = j + 4; a <= x && b >= y; a++, b--) {
			if (a < 0 || b >= 14)
				continue;
			// 좌하향 ○●●●○ 체크
			if (board[a][b].on_board == 0
					&& board[a + 1][b - 1].on_board == team
					&& board[a + 2][b - 2].on_board == team
					&& board[a + 3][b - 3].on_board == team
					&& board[a + 4][b - 4].on_board == 0) {
				count33 += 1;
				break;
			}

			// 좌하향 빈 3 체크 ○●●○●○ + 좌하향 빈 3 체크 ○●○●●○
			if (a <= 8 && b >= 6) {
				// 좌하향 빈 3 체크 ○●●○●○
				if (board[a][b].on_board == 0
						&& board[a + 1][b - 1].on_board == team
						&& board[a + 2][b - 2].on_board == team
						&& board[a + 3][b - 3].on_board == 0
						&& board[a + 4][b - 4].on_board == team
						&& board[a + 5][b - 5].on_board == 0) {
					count33 += 1;
					break;
				}
				// 좌하향 빈 3 체크 ○●○●●○
				if (board[a][b].on_board == 0
						&& board[a + 1][b - 1].on_board == team
						&& board[a + 2][b - 2].on_board == 0
						&& board[a + 3][b - 3].on_board == team
						&& board[a + 4][b - 4].on_board == team
						&& board[a + 5][b - 5].on_board == 0) {
					count33 += 1;
					break;
				}
			}
		}

		board[i][j].on_board = 0; // 함수 호출 종료 전으로 복구.
		if (count33 >= 2)
			return true; // 33 조건 때문에 둘 수 없다.
		else
			return false; // 33 조건 신경쓰지 않고 둘 수 있다.
	}

	private int canwewin(int i, int j) { // (i,j)에 두었을 때 이길 수 있는가?
		// 승리조건 검사하면서 6목 여부도 확인을 해야함.
		int k, l, status = 1;
		// 가로승리 가능?
		for (k = Math.max(j - 4, 0); k <= Math.min(j, 10); k++) {

			// 가로 연속 5개 완성 가능한 경우 발견.
			if (board[i][k].on_board + board[i][k + 1].on_board
					+ board[i][k + 2].on_board + board[i][k + 3].on_board
					+ board[i][k + 4].on_board == 4) {
				if (k - 1 >= 0)
					if (board[i][k - 1].on_board == 1)
						status = -1; // 가로 6목 방지
				if (k + 5 <= 14)
					if (board[i][k + 5].on_board == 1)
						status = -1; // 가로 6목 방지
				return 0; // 가로 5 승리 가능.
			}
		}
		// 세로 승리 가능?
		for (k = Math.max(i - 4, 0); k <= Math.min(i, 10); k++) {
			if (board[k][j].on_board + board[k + 1][j].on_board
					+ board[k + 2][j].on_board + board[k + 3][j].on_board
					+ board[k + 4][j].on_board == 4) {
				if (k - 1 >= 0)
					if (board[k - 1][j].on_board == 1)
						status = -1; // 세로 6목 방지
				if (k + 5 <= 14)
					if (board[k + 5][j].on_board == 1)
						status = -1; // 세로 6목 방지
				return 0; // 세로 5 승리 가능.
			}
		}
		// 우하향 승리 가능?
		for (k = i - 4, l = j - 4; k <= i && l <= j; k++, l++) {
			if (k < 0 || l < 0)
				continue;
			if (k > 10 || l > 10)
				break;
			if (board[k][l].on_board + board[k + 1][l + 1].on_board
					+ board[k + 2][l + 2].on_board
					+ board[k + 3][l + 3].on_board
					+ board[k + 4][l + 4].on_board == 4) {
				if (k - 1 >= 0 && l - 1 >= 0)
					if (board[k - 1][l - 1].on_board == 1)
						status = -1; // 우하향 6목 방지
				if (k + 5 <= 14 && l + 5 <= 14)
					if (board[k + 5][l + 5].on_board == 1)
						status = -1; // 우하향 6목 방지
				return 0; // 우하향 5 승리 가능.
			}
		}
		// 좌하향 승리 가능?
		for (k = i - 4, l = j + 4; k <= i && l <= j; k++, l--) {
			if (k < 0 || l > 14)
				continue;
			if (k > 10 || l < 0)
				break;
			if (board[k][l].on_board + board[k + 1][l - 1].on_board
					+ board[k + 2][l - 2].on_board
					+ board[k + 3][l - 3].on_board
					+ board[k + 4][l - 4].on_board == 4) {
				if (k - 1 >= 0 && l + 1 <= 14)
					if (board[k - 1][l + 1].on_board == 1)
						status = -1; // 좌하향 6목 방지
				if (k + 5 <= 14 && l - 5 >= 0)
					if (board[k + 5][l - 5].on_board == 1)
						status = -1; // 좌하향 6목 방지
				return 0; // 좌하향 5 승리 가능.
			}
		}


		return 2; // victory not available for this position.
	}

	private int mustdefend(int i, int j) { // 흑이 방어해야하는 곳이 존재하는지 확인.

		// 방어조건 검사하면서 6목 여부도 확인을 해야함. 6목인 경우 방어 안 해도 됨.
		int k, l;
		// 흑 가로 4?
		for (k = Math.max(j - 4, 0); k <= Math.min(j, 10); k++) {
			if (board[i][k].on_board + board[i][k + 1].on_board
					+ board[i][k + 2].on_board + board[i][k + 3].on_board
					+ board[i][k + 4].on_board == -4) { // 가로 연속 5개 완성 가능한 경우
														// 발견.
				if (k - 1 >= 0)
					if (board[i][k - 1].on_board == -1)
						return -1; // 가로 6목 방지
				if (k + 5 <= 14)
					if (board[i][k + 5].on_board == -1)
						return -1; // 가로 6목 방지
				return 0; // 가로 5 방어 해야 함.
			}
		}
		// 흑 세로 4?
		for (k = Math.max(i - 4, 0); k <= Math.min(i, 10); k++) {
			if (board[k][j].on_board + board[k + 1][j].on_board
					+ board[k + 2][j].on_board + board[k + 3][j].on_board
					+ board[k + 4][j].on_board == -4) {
				if (k - 1 >= 0)
					if (board[k - 1][j].on_board == -1)
						return -1; // 세로 6목 방지
				if (k + 5 <= 14)
					if (board[k + 5][j].on_board == -1)
						return -1; // 세로 6목 방지
				return 0; // 세로 5 방어 해야 함.
			}
		}
		// 흑 우하향 4?
		for (k = i - 4, l = j - 4; k <= i && l <= j; k++, l++) {
			if (k < 0 || l < 0)
				continue;
			if (k > 10 || l > 10)
				break;
			if (board[k][l].on_board + board[k + 1][l + 1].on_board
					+ board[k + 2][l + 2].on_board
					+ board[k + 3][l + 3].on_board
					+ board[k + 4][l + 4].on_board == -4) {
				if (k - 1 >= 0 && l - 1 >= 0)
					if (board[k - 1][l - 1].on_board == -1)
						return -1; // 우하향 6목 방지
				if (k + 5 <= 14 && l + 5 <= 14)
					if (board[k + 5][l + 5].on_board == -1)
						return -1; // 우하향 6목 방지
				return 0; // 우하향 5 방어 해야 함.
			}
		}
		// 흑 좌햐향 4?
		for (k = i - 4, l = j + 4; k <= i && l <= j; k++, l--) {
			if (k < 0 || l > 14)
				continue;
			if (k > 10 || l < 0)
				break;
			if (board[k][l].on_board + board[k + 1][l - 1].on_board
					+ board[k + 2][l - 2].on_board
					+ board[k + 3][l - 3].on_board
					+ board[k + 4][l - 4].on_board == -4) {
				if (k - 1 >= 0 && l + 1 <= 14)
					if (board[k - 1][l + 1].on_board == -1)
						return  -1; // 좌하향 6목 방지
				if (k + 5 <= 14 && l - 5 >= 0)
					if (board[k + 5][l - 5].on_board == -1)
						return  -1; // 좌하향 6목 방지
				return 0; // 좌하향 5 방어 해야함.
			}
		}

		
		return -1; // 이 좌표를 반드시 방어할 필요는 없음.
	}

	private int recursive(int turns, int last_row, int last_col) { // turns 만큼의
																	// 턴을 미리
																	// 내다본다.

		int[] first = new int[3], second = new int[3];
		LinkedList candidates = new LinkedList(); // 다음 탐색할 후보 위치들 저장.

		if (turns == 0) { // 미리 내다보기로 한 턴 수만큼 도달함.

		} else {
			for (int i = Math.max(0, upbound - 3); i <= Math.min(14,
					downbound + 3); i++) {
				for (int j = Math.max(0, leftbound - 3); j <= Math.min(14,
						rightbound + 3); j++) {
					if (board[i][j].on_board != 0)
						continue; // 해당 좌표가 돌을 놓을 수 없는 경우 스킵.
					else { // 좌표가 비어있는 경우만 고려한다.

						if (check33(i, j, 1)) // 33체크
							continue;

						int win_result = canwewin(i, j); // 승리 조건 검사
						switch (win_result) {
						case 0: // 반드시 이길 수 있는 경우(5 완성 필승)
							max_a = i;
							max_b = j;
							return -1;
						case 1:

						case 2:

						}

						if (mustdefend(i, j)) { // 방어 조건 검사
							max_a = i;
							max_b = j;
							max = 100; // 다른 승리조건이 없는 이상 무조건 방어.
							continue;
						}
						// else heuristic search.

					}
				}
			}
		}

		// 함수 호출 종료 전에 한 턴 전 상태로 오목판을 돌려놓는다.

		return 1;
	}

	private void chaksoo_black(int row, int col) { // 정해진 그 위치에 흑돌 둔다 + 가중치 갱신
		board[row][col].on_board = 1;
		board[row][col].icon = '○';
		update();
		printboardstat();
		check_row_victory(1);
		check_col_victory(1);
		check_diag_victory(1);
		System.out.println("흑 선수가 한 수 두었습니다.\n");
	}

	private void chaksoo_white(int row, int col) { // 정해진 그 위치에 백돌 둔다 + 가중치 갱신
		board[row][col].on_board = -1;
		board[row][col].icon = '●';
		update();
		printboardstat();
		check_row_victory(-1);
		check_col_victory(-1);
		check_diag_victory(-1);
		System.out.println("백 선수가 한 수 두었습니다.\n");
	}

	private void update() { // 사람과 AI가 돌을 둘 때마다 각 점의 메타정보 갱신.
		// 보드 전체 다 갱신 안하도록 조정 좀 하자... 패치 점..
		update_blackweight();
		update_whiteweight();
		// 각 점의 흑 가중치, 백 가중치를 업데이트 해주었으니 각 점의 value(=흑 가중치+백 가중치)를 갱신해준다.
		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++) {
				board[i][j].value = board[i][j].black_weight
						+ board[i][j].white_weight; // value 갱신.
			}
	}

	private void update_blackweight() {

		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++) {
				if (board[i][j].on_board != 0)
					continue; // 메타 정보 갱신은 보드의 비어있는 칸들에 대해서만 진행.
				board[i][j].black_weight = 0;
				// 가로 체크
				if (j <= 10) {
					if (board[i][j].on_board != -1
							&& board[i][j + 1].on_board != -1
							&& board[i][j + 2].on_board != -1
							&& board[i][j + 3].on_board != -1
							&& board[i][j + 4].on_board != -1) {
						board[i][j].black_weight++;
						board[i][j + 1].black_weight++;
						board[i][j + 2].black_weight++;
						board[i][j + 3].black_weight++;
						board[i][j + 4].black_weight++;
					}
				}
				// 세로 체크
				if (i <= 10) {
					if (board[i][j].on_board != -1
							&& board[i + 1][j].on_board != -1
							&& board[i + 2][j].on_board != -1
							&& board[i + 3][j].on_board != -1
							&& board[i + 4][j].on_board != -1) {
						board[i][j].black_weight++;
						board[i + 1][j].black_weight++;
						board[i + 2][j].black_weight++;
						board[i + 3][j].black_weight++;
						board[i + 4][j].black_weight++;
					}
				}
				// 우하향 대각선 체크
				if (i <= 10 && j <= 10) {
					if (board[i][j].on_board != -1
							&& board[i + 1][j + 1].on_board != -1
							&& board[i + 2][j + 2].on_board != -1
							&& board[i + 3][j + 3].on_board != -1
							&& board[i + 4][j + 4].on_board != -1) {
						board[i][j].black_weight++;
						board[i + 1][j + 1].black_weight++;
						board[i + 2][j + 2].black_weight++;
						board[i + 3][j + 3].black_weight++;
						board[i + 4][j + 4].black_weight++;
					}
				}
				// 좌하향 대각선 체크
				if (j >= 4 && i <= 10) {
					if (board[i][j].on_board != -1
							&& board[i + 1][j - 1].on_board != -1
							&& board[i + 2][j - 2].on_board != -1
							&& board[i + 3][j - 3].on_board != -1
							&& board[i + 4][j - 4].on_board != -1) {
						board[i][j].black_weight++;
						board[i + 1][j - 1].black_weight++;
						board[i + 2][j - 2].black_weight++;
						board[i + 3][j - 3].black_weight++;
						board[i + 4][j - 4].black_weight++;
					}
				}
			}
	}

	private void update_whiteweight() {
		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++) {
				if (board[i][j].on_board != 0)
					continue; // 메타 정보 갱신은 보드의 비어있는 칸들에 대해서만 진행.
				board[i][j].white_weight = 0;
				// 가로 체크
				if (j <= 10) {
					if (board[i][j].on_board != 1
							&& board[i][j + 1].on_board != 1
							&& board[i][j + 2].on_board != 1
							&& board[i][j + 3].on_board != 1
							&& board[i][j + 4].on_board != 1) {
						board[i][j].white_weight++;
						board[i][j + 1].white_weight++;
						board[i][j + 2].white_weight++;
						board[i][j + 3].white_weight++;
						board[i][j + 4].white_weight++;
					}
				}
				// 세로 체크
				if (i <= 10) {
					if (board[i][j].on_board != 1
							&& board[i + 1][j].on_board != 1
							&& board[i + 2][j].on_board != 1
							&& board[i + 3][j].on_board != 1
							&& board[i + 4][j].on_board != 1) {
						board[i][j].white_weight++;
						board[i + 1][j].white_weight++;
						board[i + 2][j].white_weight++;
						board[i + 3][j].white_weight++;
						board[i + 4][j].white_weight++;
					}
				}
				// 우하향 대각선 체크
				if (i <= 10 && j <= 10) {
					if (board[i][j].on_board != 1
							&& board[i + 1][j + 1].on_board != 1
							&& board[i + 2][j + 2].on_board != 1
							&& board[i + 3][j + 3].on_board != 1
							&& board[i + 4][j + 4].on_board != 1) {
						board[i][j].white_weight++;
						board[i + 1][j + 1].white_weight++;
						board[i + 2][j + 2].white_weight++;
						board[i + 3][j + 3].white_weight++;
						board[i + 4][j + 4].white_weight++;
					}
				}
				// 좌하향 대각선 체크
				if (j >= 4 && i <= 10) {
					if (board[i][j].on_board != 1
							&& board[i + 1][j - 1].on_board != 1
							&& board[i + 2][j - 2].on_board != 1
							&& board[i + 3][j - 3].on_board != 1
							&& board[i + 4][j - 4].on_board != 1) {
						board[i][j].white_weight++;
						board[i + 1][j - 1].white_weight++;
						board[i + 2][j - 2].white_weight++;
						board[i + 3][j - 3].white_weight++;
						board[i + 4][j - 4].white_weight++;
					}
				}
			}
	}

	public void initiate_game() {

		printboardstat();
		chaksoo_black(6, 7); // 흑이 두는 것으로 시작.

		// while()
	}
}

public class fifteen {
	public static void main(String args[]) {
		omok15 new_5mok = new omok15();
		new_5mok.initiate_game();
	}
}