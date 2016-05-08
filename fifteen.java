package fifteen;

import java.util.*;

class point {
	char col;
	int row;

	int on_board = 0;// 0�̸� �� ���� ��. 1�̸� ��, -1�̸� ��.
	int black_weight = 0, white_weight = 0, value;// value��
													// black_weight+white_weight
	char icon; // ȭ�鿡 ǥ�õǴ� ���� char.

	public point(int col, int row, char icon) {
		this.col = (char) col;
		this.row = row;
		this.icon = icon;
	}

}

class loc {
	int row, col, score = 0; // score: ���� �� �ĺ��� ���ϴ� �� �̿��ϴ� ����.

	public loc(int row, int col) {
		this.row = row;
		this.col = col;
	}
}

class omok15 {
	point[][] board;
	int count = 0; // turn count.
	Scanner keyboard;

	int leftbound = 0, rightbound = 14; // �������� ���� ���ʿ� ��ġ�� �浹�� ���� �����ʿ� ��ġ�� �浹��
										// col
	int upbound = 0, downbound = 14; // �������� ���� ���ʿ� ��ġ�� �浹�� ���� �Ʒ��ʿ� ��ġ�� �浹��
										// row

	int alpha, beta; // variables used for alpha-beta pruning.
	int a = 0, b = 0, max_a = -1, max_b = -1, max = -1;

	public omok15() { // omok15 Ŭ���� ������. ���� metadata �ʱ�ȭ.

		keyboard = new Scanner(System.in);
		board = new point[15][15];

		// ù ��
		board[0][0] = new point(65, 15, '��');
		for (int i = 1; i < 14; i++)
			board[0][i] = new point(65 + i, 5, '��');
		board[0][14] = new point(79, 15, '��');

		// �߰� 13��
		for (int i = 1; i <= 13; i++) {
			for (int j = 0; j < 15; j++) {
				if (j == 0) {
					board[i][j] = new point(65 + j, 15 - i, '��');
				} else if (j == 14) {
					board[i][j] = new point(65 + j, 15 - i, '��');
				} else {
					board[i][j] = new point(65 + j, 15 - i, '��');
				}
			}
		}

		// ������ ��
		board[14][0] = new point(65, 1, '��');
		for (int i = 1; i < 14; i++)
			board[0][i] = new point(65 + i, 1, '��');
		board[14][14] = new point(79, 1, '��');

		// ����ġ �ʱ�ȭ (black_weight, white_Weight)
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				// ���� üũ
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
				// ���� üũ
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
				// ������ �밢�� üũ
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
				// ������ �밢�� üũ
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

		// value �ʱ�ȭ (black_weight+white_weight)
		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++)
				board[i][j].value = board[i][j].black_weight
						+ board[i][j].white_weight;
	}

	private void victory_endgame(int team) {
		if (team == 1) { // ��� �¸�
			System.out.println("\n�� ����(A.I.)�� �¸��Դϴ�. GAME OVER.");
			System.exit(0);
		} else if (team == -1) { // ��� �¸�
			System.out.println("\n�� ����(A.I.)�� �¸��Դϴ�. VICTORY.");
			System.exit(0);
		}
	}

	private void check_row_victory(int team) { // �� �¸����� �˻�
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

	private void check_col_victory(int team) { // �� �¸����� �˻�
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

	private void check_diag_victory(int team) { // �밢�� �¸����� �˻�

		// ������ �밢�� �¸����� �˻�
		for (int i = 0; i <= 10; i++) {
			for (int j = 0; j <= 10; j++) {
				if (board[i][j].on_board + board[i + 1][j + 1].on_board
						+ board[i + 2][j + 2].on_board
						+ board[i + 3][j + 3].on_board
						+ board[i + 4][j + 4].on_board == 5)
					victory_endgame(team);
			}
		}
		// ������ �밢�� �¸����� �˻�
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

	public void printboardstat() { // �������� �� ���¸� ����մϴ�.
		System.out.println("   A B C D E F G H I J K L M N O");

		for (int i = 0; i < 5; i++) {
			System.out.print((15 - i) + " "); // �� ��ȣ ��� (10~15)
			for (int j = 0; j < 14; j++)
				System.out.print(board[i][j].icon + "��");
			System.out.println(board[i][14].icon);
		}

		for (int i = 5; i < 15; i++) {
			System.out.print(" " + (15 - i) + " "); // �� ��ȣ ��� (1~9). �ڸ��� ������.
			for (int j = 0; j < 14; j++)
				System.out.print(board[i][j].icon + "��");
			System.out.println(board[i][14].icon);
		}

	}

	public void hansoo_black() { // recursive �Լ��� ȣ���Ͽ� ������ �� ���� �����Ѵ�.
		int foresee = 7; // foresee��ŭ ���� ���ٺ���.
		int finalist_row, finalist_col, max_score;
		LinkedList candidates = new LinkedList(); // ���� Ž���� �ĺ� ��ġ�� ����.

		// recursive(foresee);
		// chaksoo_black(finalist_row, finalist_col);
	}

	public void hansoo_white() {
		while (true) {
			System.out.println("�� ���� �� ��ǥ�� �������ּ���.");
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
			else { // ��ȿ���� ���� �� �Է½� ���û.
				System.out.println("Wrong input");
				System.out.println("enter valid input.Try again.");
				continue;
			}
			int row = Character.getNumericValue(white_input.charAt(1));
			if (row > 15 || row < 1) // ��ȿ���� ���� �� �Է½� ���û.
			{
				System.out.println("enter valid input.Try again.");
				continue;
			}
			// �ش� ��ġ�� ���� �ִ��� �˻�
			if (board[15 - row][col].on_board != 0) {
				System.out
						.println("a stone is already in the position.\nTry again.");
				continue;
			}

			// 33 ���� �˻� (����� �Է�)
			if (check33(15 - row, col, -1)) {
				System.out.println("33 ��ġ�� �� �� �����ϴ�. ��ǥ�� ���Է����ּ���\n");
				continue; // 33�� �߻��ϴ� ��ġ�̹Ƿ� ���� �ʴ´�.
			}

			// �Էµ� ��ġ�� �� �� �α� (�� ����)
			chaksoo_white(15 - row, col);
			break;
		}
	}

	private boolean check33(int i, int j, int team) {
		int count33 = 0; // �� 3 ����
		int a, b, x, y;
		board[i][j].on_board = team; // temporarily

		// ���� �� 3 count
		a = i;
		x = Math.max(0, j - 4);
		y = Math.min(9, j - 1);
		for (b = x; b <= y; b++) {
			// ���� ���� �� 3 üũ �ۡܡܡܡ�
			if (board[a][b].on_board == 0 && board[a][b + 1].on_board == team
					&& board[a][b + 2].on_board == team
					&& board[a][b + 3].on_board == team
					&& board[a][b + 4].on_board == 0) {
				count33 += 1;
				break;
			}
			// ���� �� 3 üũ �ۡܡܡۡܡ� + ���� �� 3 üũ �ۡܡۡܡܡ�
			if (b <= 8) {
				// üũ �ۡܡܡۡܡ�
				if (board[a][b].on_board == 0
						&& board[a][b + 1].on_board == team
						&& board[a][b + 2].on_board == team
						&& board[a][b + 3].on_board == 0
						&& board[a][b + 4].on_board == team
						&& board[a][b + 5].on_board == 0) {
					count33 += 1;
					break;
				}
				// üũ �ۡܡۡܡܡ�
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

		// ���� 33 üũ
		b = j;
		x = Math.max(0, i - 4);
		y = Math.min(9, i - 1);
		for (a = x; a <= y; a++) {
			// ���� ���� �� 3 üũ �ۡܡܡܡ�
			if (board[a][b].on_board == 0 && board[a + 1][b].on_board == team
					&& board[a + 2][b].on_board == team
					&& board[a + 3][b].on_board == team
					&& board[a + 4][b].on_board == 0) {
				count33 += 1;
				break;
			}
			// ���� �� 3 üũ �ۡܡܡۡܡ� + ���� �� 3 üũ �ۡܡۡܡܡ�
			if (a <= 8) {
				// üũ �ۡܡܡۡܡ�
				if (board[a][b].on_board == 0
						&& board[a + 1][b].on_board == team
						&& board[a + 2][b].on_board == team
						&& board[a + 3][b].on_board == 0
						&& board[a + 4][b].on_board == team
						&& board[a + 5][b].on_board == 0) {
					count33 += 1;
					break;
				}
				// üũ �ۡܡۡܡܡ�
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

		// ������ 33 üũ
		x = Math.min(9, i - 1);
		y = Math.min(9, j - 1);
		for (a = i - 4, b = j - 4; a <= x && b <= y; a++, b++) {
			if (a < 0 || b < 0)
				continue;
			// ������ �ۡܡܡܡ� üũ
			if (board[a][b].on_board == 0
					&& board[a + 1][b + 1].on_board == team
					&& board[a + 2][b + 2].on_board == team
					&& board[a + 3][b + 3].on_board == team
					&& board[a + 4][b + 4].on_board == 0) {
				count33 += 1;
				break;
			}

			// ������ �� 3 üũ �ۡܡܡۡܡ� + ������ �� 3 üũ �ۡܡۡܡܡ�
			if (a <= 8 && b <= 8) {
				// ������ �� 3 üũ �ۡܡܡۡܡ�
				if (board[a][b].on_board == 0
						&& board[a + 1][b + 1].on_board == team
						&& board[a + 2][b + 2].on_board == team
						&& board[a + 3][b + 3].on_board == 0
						&& board[a + 4][b + 4].on_board == team
						&& board[a + 5][b + 5].on_board == 0) {
					count33 += 1;
					break;
				}
				// ������ �� 3 üũ �ۡܡۡܡܡ�
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

		// ������ 33 üũ
		x = Math.min(9, i - 1);
		y = Math.max(5, j + 1);
		for (a = i - 4, b = j + 4; a <= x && b >= y; a++, b--) {
			if (a < 0 || b >= 14)
				continue;
			// ������ �ۡܡܡܡ� üũ
			if (board[a][b].on_board == 0
					&& board[a + 1][b - 1].on_board == team
					&& board[a + 2][b - 2].on_board == team
					&& board[a + 3][b - 3].on_board == team
					&& board[a + 4][b - 4].on_board == 0) {
				count33 += 1;
				break;
			}

			// ������ �� 3 üũ �ۡܡܡۡܡ� + ������ �� 3 üũ �ۡܡۡܡܡ�
			if (a <= 8 && b >= 6) {
				// ������ �� 3 üũ �ۡܡܡۡܡ�
				if (board[a][b].on_board == 0
						&& board[a + 1][b - 1].on_board == team
						&& board[a + 2][b - 2].on_board == team
						&& board[a + 3][b - 3].on_board == 0
						&& board[a + 4][b - 4].on_board == team
						&& board[a + 5][b - 5].on_board == 0) {
					count33 += 1;
					break;
				}
				// ������ �� 3 üũ �ۡܡۡܡܡ�
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

		board[i][j].on_board = 0; // �Լ� ȣ�� ���� ������ ����.
		if (count33 >= 2)
			return true; // 33 ���� ������ �� �� ����.
		else
			return false; // 33 ���� �Ű澲�� �ʰ� �� �� �ִ�.
	}

	private int canwewin(int i, int j) { // (i,j)�� �ξ��� �� �̱� �� �ִ°�?
		// �¸����� �˻��ϸ鼭 6�� ���ε� Ȯ���� �ؾ���.
		int k, l, status = 1;
		// ���ν¸� ����?
		for (k = Math.max(j - 4, 0); k <= Math.min(j, 10); k++) {

			// ���� ���� 5�� �ϼ� ������ ��� �߰�.
			if (board[i][k].on_board + board[i][k + 1].on_board
					+ board[i][k + 2].on_board + board[i][k + 3].on_board
					+ board[i][k + 4].on_board == 4) {
				if (k - 1 >= 0)
					if (board[i][k - 1].on_board == 1)
						status = -1; // ���� 6�� ����
				if (k + 5 <= 14)
					if (board[i][k + 5].on_board == 1)
						status = -1; // ���� 6�� ����
				return 0; // ���� 5 �¸� ����.
			}
		}
		// ���� �¸� ����?
		for (k = Math.max(i - 4, 0); k <= Math.min(i, 10); k++) {
			if (board[k][j].on_board + board[k + 1][j].on_board
					+ board[k + 2][j].on_board + board[k + 3][j].on_board
					+ board[k + 4][j].on_board == 4) {
				if (k - 1 >= 0)
					if (board[k - 1][j].on_board == 1)
						status = -1; // ���� 6�� ����
				if (k + 5 <= 14)
					if (board[k + 5][j].on_board == 1)
						status = -1; // ���� 6�� ����
				return 0; // ���� 5 �¸� ����.
			}
		}
		// ������ �¸� ����?
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
						status = -1; // ������ 6�� ����
				if (k + 5 <= 14 && l + 5 <= 14)
					if (board[k + 5][l + 5].on_board == 1)
						status = -1; // ������ 6�� ����
				return 0; // ������ 5 �¸� ����.
			}
		}
		// ������ �¸� ����?
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
						status = -1; // ������ 6�� ����
				if (k + 5 <= 14 && l - 5 >= 0)
					if (board[k + 5][l - 5].on_board == 1)
						status = -1; // ������ 6�� ����
				return 0; // ������ 5 �¸� ����.
			}
		}


		return 2; // victory not available for this position.
	}

	private int mustdefend(int i, int j) { // ���� ����ؾ��ϴ� ���� �����ϴ��� Ȯ��.

		// ������� �˻��ϸ鼭 6�� ���ε� Ȯ���� �ؾ���. 6���� ��� ��� �� �ص� ��.
		int k, l;
		// �� ���� 4?
		for (k = Math.max(j - 4, 0); k <= Math.min(j, 10); k++) {
			if (board[i][k].on_board + board[i][k + 1].on_board
					+ board[i][k + 2].on_board + board[i][k + 3].on_board
					+ board[i][k + 4].on_board == -4) { // ���� ���� 5�� �ϼ� ������ ���
														// �߰�.
				if (k - 1 >= 0)
					if (board[i][k - 1].on_board == -1)
						return -1; // ���� 6�� ����
				if (k + 5 <= 14)
					if (board[i][k + 5].on_board == -1)
						return -1; // ���� 6�� ����
				return 0; // ���� 5 ��� �ؾ� ��.
			}
		}
		// �� ���� 4?
		for (k = Math.max(i - 4, 0); k <= Math.min(i, 10); k++) {
			if (board[k][j].on_board + board[k + 1][j].on_board
					+ board[k + 2][j].on_board + board[k + 3][j].on_board
					+ board[k + 4][j].on_board == -4) {
				if (k - 1 >= 0)
					if (board[k - 1][j].on_board == -1)
						return -1; // ���� 6�� ����
				if (k + 5 <= 14)
					if (board[k + 5][j].on_board == -1)
						return -1; // ���� 6�� ����
				return 0; // ���� 5 ��� �ؾ� ��.
			}
		}
		// �� ������ 4?
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
						return -1; // ������ 6�� ����
				if (k + 5 <= 14 && l + 5 <= 14)
					if (board[k + 5][l + 5].on_board == -1)
						return -1; // ������ 6�� ����
				return 0; // ������ 5 ��� �ؾ� ��.
			}
		}
		// �� ������ 4?
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
						return  -1; // ������ 6�� ����
				if (k + 5 <= 14 && l - 5 >= 0)
					if (board[k + 5][l - 5].on_board == -1)
						return  -1; // ������ 6�� ����
				return 0; // ������ 5 ��� �ؾ���.
			}
		}

		
		return -1; // �� ��ǥ�� �ݵ�� ����� �ʿ�� ����.
	}

	private int recursive(int turns, int last_row, int last_col) { // turns ��ŭ��
																	// ���� �̸�
																	// ���ٺ���.

		int[] first = new int[3], second = new int[3];
		LinkedList candidates = new LinkedList(); // ���� Ž���� �ĺ� ��ġ�� ����.

		if (turns == 0) { // �̸� ���ٺ���� �� �� ����ŭ ������.

		} else {
			for (int i = Math.max(0, upbound - 3); i <= Math.min(14,
					downbound + 3); i++) {
				for (int j = Math.max(0, leftbound - 3); j <= Math.min(14,
						rightbound + 3); j++) {
					if (board[i][j].on_board != 0)
						continue; // �ش� ��ǥ�� ���� ���� �� ���� ��� ��ŵ.
					else { // ��ǥ�� ����ִ� ��츸 ����Ѵ�.

						if (check33(i, j, 1)) // 33üũ
							continue;

						int win_result = canwewin(i, j); // �¸� ���� �˻�
						switch (win_result) {
						case 0: // �ݵ�� �̱� �� �ִ� ���(5 �ϼ� �ʽ�)
							max_a = i;
							max_b = j;
							return -1;
						case 1:

						case 2:

						}

						if (mustdefend(i, j)) { // ��� ���� �˻�
							max_a = i;
							max_b = j;
							max = 100; // �ٸ� �¸������� ���� �̻� ������ ���.
							continue;
						}
						// else heuristic search.

					}
				}
			}
		}

		// �Լ� ȣ�� ���� ���� �� �� �� ���·� �������� �������´�.

		return 1;
	}

	private void chaksoo_black(int row, int col) { // ������ �� ��ġ�� �浹 �д� + ����ġ ����
		board[row][col].on_board = 1;
		board[row][col].icon = '��';
		update();
		printboardstat();
		check_row_victory(1);
		check_col_victory(1);
		check_diag_victory(1);
		System.out.println("�� ������ �� �� �ξ����ϴ�.\n");
	}

	private void chaksoo_white(int row, int col) { // ������ �� ��ġ�� �鵹 �д� + ����ġ ����
		board[row][col].on_board = -1;
		board[row][col].icon = '��';
		update();
		printboardstat();
		check_row_victory(-1);
		check_col_victory(-1);
		check_diag_victory(-1);
		System.out.println("�� ������ �� �� �ξ����ϴ�.\n");
	}

	private void update() { // ����� AI�� ���� �� ������ �� ���� ��Ÿ���� ����.
		// ���� ��ü �� ���� ���ϵ��� ���� �� ����... ��ġ ��..
		update_blackweight();
		update_whiteweight();
		// �� ���� �� ����ġ, �� ����ġ�� ������Ʈ ���־����� �� ���� value(=�� ����ġ+�� ����ġ)�� �������ش�.
		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++) {
				board[i][j].value = board[i][j].black_weight
						+ board[i][j].white_weight; // value ����.
			}
	}

	private void update_blackweight() {

		for (int i = 0; i < 15; i++)
			for (int j = 0; j < 15; j++) {
				if (board[i][j].on_board != 0)
					continue; // ��Ÿ ���� ������ ������ ����ִ� ĭ�鿡 ���ؼ��� ����.
				board[i][j].black_weight = 0;
				// ���� üũ
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
				// ���� üũ
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
				// ������ �밢�� üũ
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
				// ������ �밢�� üũ
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
					continue; // ��Ÿ ���� ������ ������ ����ִ� ĭ�鿡 ���ؼ��� ����.
				board[i][j].white_weight = 0;
				// ���� üũ
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
				// ���� üũ
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
				// ������ �밢�� üũ
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
				// ������ �밢�� üũ
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
		chaksoo_black(6, 7); // ���� �δ� ������ ����.

		// while()
	}
}

public class fifteen {
	public static void main(String args[]) {
		omok15 new_5mok = new omok15();
		new_5mok.initiate_game();
	}
}