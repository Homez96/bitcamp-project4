package bitcamp.project4.dao;

import bitcamp.project4.myapp.dao.QuizDao;
import bitcamp.project4.myapp.vo.Quiz;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ListQuizDao implements QuizDao {

  private static final String DEFAULT_DATANAME = "quizs";
  private int seqNo;
  private List<Quiz> quizList = new ArrayList<>();
  private String path;
  private String dataName;

  public ListQuizDao(String path) {
    this(path, DEFAULT_DATANAME);
  }

  public ListQuizDao(String path, String dataName) {
    this.path = path;
    this.dataName = dataName;

    try (XSSFWorkbook workbook = new XSSFWorkbook(path)) {
      XSSFSheet sheet = workbook.getSheet(dataName);

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        try {
          Quiz quiz = new Quiz();
          quiz.setNo(Integer.parseInt(row.getCell(0).getStringCellValue()));
          quiz.setNumber(Integer.parseInt(row.getCell(1).getStringCellValue()));
          quiz.setAnswer(row.getCell(2).getStringCellValue());

          quizList.add(quiz);

        } catch (Exception e) {
          System.out.printf("%s 번의 데이터 형식이 맞지 않습니다.\n", row.getCell(0).getStringCellValue());
        }
      }

      seqNo = quizList.getLast().getNo();

    } catch (Exception e) {
      System.out.println("퀴즈 데이터 로딩 중 오류 발생!");
      e.printStackTrace();
    }
  }

  public void save() throws Exception {
    try (FileInputStream in = new FileInputStream(path);
        XSSFWorkbook workbook = new XSSFWorkbook(in)) {

      int sheetIndex = workbook.getSheetIndex(dataName);
      if (sheetIndex != -1) {
        workbook.removeSheetAt(sheetIndex);
      }

      XSSFSheet sheet = workbook.createSheet(dataName);

      // 셀 이름 출력
      String[] cellHeaders = {"no", "number", "answer"};
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < cellHeaders.length; i++) {
        headerRow.createCell(i).setCellValue(cellHeaders[i]);
      }

      // 데이터 저장
      int rowNo = 1;
      for (Quiz quiz : quizList) {
        Row dataRow = sheet.createRow(rowNo++);
        dataRow.createCell(0).setCellValue(String.valueOf(quiz.getNo()));
        dataRow.createCell(1).setCellValue(String.valueOf(quiz.getNumber()));
        dataRow.createCell(2).setCellValue(quiz.getAnswer());
      }

      // 엑셀 파일로 데이터를 출력하기 전에
      // workbook을 위해 연결한 입력 스트림을 먼저 종료한다.
      in.close();

      try (FileOutputStream out = new FileOutputStream(path)) {
        workbook.write(out);
      }
    }
  }

  @Override
  public boolean insert(Quiz quiz) throws Exception {
    quiz.setNo(++seqNo);
    quizList.add(quiz);
    return true;
  }

  @Override
  public List<Quiz> list() throws Exception {
    return quizList.stream().toList();
  }

  @Override
  public Quiz findBy(int no) throws Exception {
    for (Quiz quiz : quizList) {
      if (quiz.getNo() == no) {
        return quiz;
      }
    }
    return null;
  }

  @Override
  public boolean update(Quiz quiz) throws Exception {
    int index = quizList.indexOf(quiz);
    if (index == -1) {
      return false;
    }

    quizList.set(index, quiz);
    return true;
  }

  @Override
  public boolean delete(int no) throws Exception {
    int index = quizList.indexOf(new Quiz(no));
    if (index == -1) {
      return false;
    }

    quizList.remove(index);
    return true;
  }


}