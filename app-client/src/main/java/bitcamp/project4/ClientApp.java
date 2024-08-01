package bitcamp.project4;

import bitcamp.project4.context.ApplicationContext;
import bitcamp.project4.listener.ApplicationListener;
import bitcamp.project4.listener.InitApplicationListener;
import bitcamp.project4.util.Prompt;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientApp {

  List<ApplicationListener> listeners = new ArrayList<>();
  ApplicationContext appCtx = new ApplicationContext();
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;

  public static void main(String[] args) {
    ClientApp app = new ClientApp();
    // 애플리케이션이 시작되거나 종료될 때 알림 받을 객체의 연락처를 등록한다.
    app.addApplicationListener(new InitApplicationListener());
    app.execute();
  }

  private void addApplicationListener(ApplicationListener listener) {
    listeners.add(listener);
  }

  private void removeApplicationListener(ApplicationListener listener) {
    listeners.remove(listener);
  }

  void execute() {

    try {
      System.out.println("Default [ localhost / 8888 ]");
      appCtx.setAttribute("host", Prompt.input("서버 주소?"));
      appCtx.setAttribute("port", Prompt.inputInt("포트 번호?"));
      // 클라이언트 소켓 생성
      socket = new Socket((String) appCtx.getAttribute("host"), (int) appCtx.getAttribute("port"));

      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      // 애플리케이션이 시작될 때 리스너에게 알린다.
      for (ApplicationListener listener : listeners) {
        try {
          listener.onStart(appCtx);
        } catch (Exception e) {
          System.out.println("리스너 실행 중 오류 발생!");
        }
      }

      // 게임 시작 메뉴 전에 환영 메시지 출력
      System.out.println("---------------------------------");
      System.out.println("[Welcome to Hang Man Game! \uD83C\uDFAE]");
      System.out.println("---------------------------------");

      while (true) {
        // 매 턴 시작 시 타이틀과 구분선을 출력
        System.out.println("---------------------------------");
        System.out.println("Hang Man Game 🎮");
        System.out.println("---------------------------------");
        String command = Prompt.input("1)게임시작 2)종료 > ");
        if (command.equals("1")) {
          playHangman();

        } else if (command.equals("2")) {
          break;
        }
      }

    } catch (Exception ex) {
      System.out.println("실행 오류!");
      ex.printStackTrace();
    } finally {
      closeConnection();
    }

    System.out.println("종료합니다.");

    Prompt.close();

    // 애플리케이션이 종료될 때 리스너에게 알린다.
    for (ApplicationListener listener : listeners) {
      try {
        listener.onShutdown(appCtx);
      } catch (Exception e) {
        System.out.println("리스너 실행 중 오류 발생!");
      }
    }
  }

  private void playHangman() throws Exception {
    out.writeUTF("hangman");
    out.flush();

    int wordLength = (int) in.readObject();
    int turnsLeft = in.readInt();
    String topic = (String) in.readObject();
    String gameState = (String) in.readObject();

    System.out.println("행맨 게임을 시작합니다!");
    System.out.println("주제: " + topic);
    System.out.println("단어 길이: " + wordLength);
    System.out.println(gameState);

    while (true) {
      char guess;
      while (true) {
        System.out.print("글자를 추측하세요: ");
        String input = Prompt.input("").trim().toLowerCase();

        if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
          guess = input.charAt(0);
          break;
        } else {
          System.out.println("잘못된 입력입니다. 알파벳 하나만 입력해주세요.");
        }
      }

      out.writeChar(guess);
      out.flush();

      boolean correctGuess = in.readBoolean();
      turnsLeft = in.readInt();
      String currentWord = (String) in.readObject();
      boolean gameOver = in.readBoolean();
      gameState = (String) in.readObject();

      System.out.println(gameState);

      if (gameOver) {
        String answer = (String) in.readObject();
        boolean win = in.readBoolean();

        if (win) {
          System.out.println("축하합니다! 정답을 맞추셨습니다.");
        } else {
          System.out.println("아쉽네요. 다음에 다시 도전해보세요.");
        }
        System.out.println("정답은 '" + answer + "' 였습니다.");
        break;
      }
    }
  }

  private void closeConnection() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (socket != null) socket.close();
    } catch (Exception e) {
      System.out.println("연결 종료 중 오류 발생: " + e.getMessage());
    }
  }
}