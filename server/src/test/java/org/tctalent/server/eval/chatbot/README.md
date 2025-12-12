# Chatbot Evaluation Framework

A JUnit-based evaluation framework for testing the chatbot's response quality, knowledge base compliance, and edge case handling.

## Overview

This framework provides automated evaluation of the chatbot using test datasets and FAQ ID validation. Claude cites FAQ IDs in responses which are extracted for tracking and validation. It's designed to be run manually (not in CI) to avoid API costs, but can be integrated into CI workflows later if needed.

## Architecture

### Test Datasets (`src/test/resources/chatbot/eval/`)

Two JSON datasets define test cases:

1. **`chatbot_evaluation_dataset.json`** - Comprehensive evaluation covering all FAQ categories (22 tests)
2. **`edge_cases.json`** - Out-of-scope questions and boundary conditions (18 tests)

### Test Classes

- **`ChatbotEvaluationTest`** - Main evaluation with comprehensive coverage (80% pass rate threshold)
- **`ChatbotEdgeCaseEvaluationTest`** - Tests edge cases and out-of-scope handling (90% pass rate threshold)

### Evaluation Logic

- **`ChatbotEvaluator`** - Core evaluation logic (FAQ ID validation, scoring)
- **`EvaluationReport`** - Aggregates results and statistics
- **`EvaluationReportGenerator`** - Generates console and HTML reports
- **`FaqIdExtractor`** - Extracts FAQ citations from Claude's responses and removes them from user-facing text

## Running the Evaluations

### Prerequisites

1. **Set the Anthropic API Key**:
   ```bash
   export ANTHROPIC_API_KEY=your_api_key_here
   ```

2. **Ensure the application can start** (database connection, etc.)

### Run All Evaluations

```bash
./gradlew chatbotEval
```

This will:
- Run all three evaluation test suites
- Make real API calls to Anthropic (costs ~$0.01-0.10 per run)
- Generate console output with progress
- Create report files in `build/reports/chatbot-eval/`

### Run Specific Test Suite

```bash
# Run only golden dataset tests
./gradlew test --tests "ChatbotEvaluationTest"

# Run only edge case tests
./gradlew test --tests "ChatbotEdgeCaseEvaluationTest"

# Run only coverage tests
./gradlew test --tests "ChatbotKnowledgeCoverageTest"
```

### Exclude from Regular Tests

The evaluation tests are tagged with `@Tag("chatbot-eval")` which means:
- They **will NOT** run with `./gradlew test`
- They **will NOT** run in CI by default
- They **only** run when explicitly invoked via `./gradlew chatbotEval`

## Understanding the Results

### Console Output

During execution, you'll see progress for each test:
```
[1/12] Testing: qa_001
  Status: ✓ PASS
  Score: 1.00

[2/12] Testing: qa_002
  Status: ✗ FAIL
  Score: 0.60
  Feedback: Only found 3 of 5 expected keywords (60%) - threshold is 70%
```

### Evaluation Report

After completion, a detailed report is printed showing:
- **Overall Statistics**: Pass rate, average score, total tests
- **Results by Category**: Pass rates for each knowledge area
- **Failed Test Details**: Question, response, and specific feedback

### Report Files

Two report files are generated in `build/reports/chatbot-eval/`:

1. **Text Report** (`golden-dataset-report.txt`, etc.)
   - Plain text format with detailed results
   - Good for logging and version control

2. **HTML Report** (`chatbot-evaluation-report.html`, `edge-cases-report.html`)
   - Visual report with tables and formatting
   - Open with: `open build/reports/chatbot-eval/chatbot-evaluation-report.html`

## Evaluation Criteria

### Main Evaluation Tests

Tests pass when:
- **All expected FAQ IDs** are referenced in the bot's response
- For out-of-scope questions: **No FAQ IDs** should be referenced
- Response is not empty or an error message

How it works:
- Claude cites FAQ IDs inline (e.g., [FAQ-001]) at the end of responses
- FAQ citations are extracted and stored in database for analytics
- FAQ citations are removed from user-facing messages (hidden from UI)
- Evaluator validates that correct FAQs were referenced

Scoring:
- Score = (FAQ IDs found / expected FAQ IDs) × 1.0
- Out-of-scope questions: pass only if no FAQ IDs are present

### Edge Case Tests

Tests pass when:
- Response contains the **expected pattern** (e.g., "not smart enough")
- Indicates the bot correctly declined to answer out-of-scope questions

### Pass Rate Thresholds

- **Main Evaluation**: 80% overall pass rate required
- **Edge Cases**: 90% overall pass rate required

## Adding New Test Cases

### 1. Add to Appropriate Dataset

Edit the relevant JSON file in `src/test/resources/chatbot/eval/`:

**For main Q&A tests** - Add to `chatbot_evaluation_dataset.json`:
```json
{
  "id": "eval_023",
  "question": "Your new question here?",
  "expectedFaqIds": ["faq_001", "faq_005"],
  "category": "your_category",
  "description": "What this test validates"
}
```
Note: Map the question to the appropriate FAQ ID(s) from `chatbotQAFile.json`

**For edge cases** - Add to `edge_cases.json`:
```json
{
  "id": "edge_019",
  "question": "Out of scope question?",
  "expectedFaqIds": [],
  "expectedResponse": "not smart enough",
  "type": "out_of_scope",
  "description": "What edge case this tests"
}
```
Note: Use empty array `[]` for expectedFaqIds to indicate no FAQs should be referenced

### 2. Re-run Evaluation

```bash
./gradlew chatbotEval
```

The new test case will be automatically loaded and evaluated.

## Customizing Evaluation Logic

### Adjust Pass Rate Thresholds

Edit the threshold in the test class's `@AfterAll` method:

```java
double minimumPassRate = 0.80; // Change to desired threshold
```

### Modify FAQ ID Validation

Edit `ChatbotEvaluator.java` to change how FAQ references are evaluated:
- Current: All expected FAQ IDs must be present for test to pass
- Could allow partial credit for some FAQ IDs being present
- Could add warnings for unexpected FAQ IDs being referenced

### Allow Additional FAQ References

Currently, tests pass if expected FAQs are present (additional FAQs are noted but don't fail the test). 
To make tests stricter (exact FAQ match only), modify `evaluateFaqIds()` in `ChatbotEvaluator.java` to fail on unexpected FAQ IDs.

## Troubleshooting

### "ANTHROPIC_API_KEY is not set"

Set the environment variable:
```bash
export ANTHROPIC_API_KEY=your_key_here
```

### Application Fails to Start

Ensure database and other dependencies are available:
```bash
docker-compose -f docker-compose/docker-compose.yml up -d
```

### High Failure Rate

1. Check if the knowledge base has changed
2. Review failed test details in the report
3. Update test dataset expectations if needed
4. Consider if the chatbot prompt needs adjustment

### Tests Timeout

Increase the test timeout in the test class:
```java
@Test
@Timeout(value = 5, unit = TimeUnit.MINUTES)
void evaluateGoldenDataset() { ... }
```

## Cost Management

### Estimated Costs

- **Main Evaluation** (~22 tests): $0.02-0.05
- **Edge Cases** (~18 tests): $0.01-0.03
- **Total per run**: ~$0.03-0.08

Using Claude Haiku 4.5 (cheapest model).

### Reducing Costs

1. **Run selectively**: Only run specific test suites when needed
2. **Smaller datasets**: Remove redundant test cases
3. **Mock responses**: Use mock responses for development/testing of the eval framework itself

## Integration with CI (Future)

To add to CI later:

1. **Create GitHub Actions workflow**:
   ```yaml
   - name: Run Chatbot Evaluations
     if: contains(github.event.pull_request.labels.*.name, 'chatbot')
     env:
       ANTHROPIC_API_KEY: ${{ secrets.ANTHROPIC_API_KEY }}
     run: ./gradlew chatbotEval
   ```

2. **Add pass/fail criteria** in CI config

3. **Store reports as artifacts**:
   ```yaml
   - uses: actions/upload-artifact@v3
     with:
       name: chatbot-eval-reports
       path: server/build/reports/chatbot-eval/
   ```

## Best Practices

1. **Run before deploying chatbot changes** - Catch regressions early
2. **Update test datasets when knowledge base changes** - Add new FAQ IDs to tests as FAQs are added
3. **Review failed tests carefully** - Check if FAQ mapping is correct or if bot behavior changed
4. **Track pass rates over time** - Monitor for degradation
5. **Use FAQ analytics** - Query database to see which FAQs are most/least used
6. **Map tests to correct FAQs** - Ensure expectedFaqIds match the actual FAQ that should answer each question

## File Structure

```
server/src/test/
├── java/org/tctalent/server/eval/chatbot/
│   ├── ChatbotEvaluationTest.java
│   ├── ChatbotEdgeCaseEvaluationTest.java
│   ├── model/
│   │   ├── EvaluationTestCase.java
│   │   ├── EvaluationResult.java
│   │   └── EvaluationReport.java
│   ├── util/
│   │   ├── ChatbotEvaluator.java
│   │   └── EvaluationReportGenerator.java
│   └── README.md (this file)
└── resources/chatbot/eval/
    ├── chatbot_evaluation_dataset.json
    └── edge_cases.json
```

## Questions or Issues?

Contact the development team or open an issue in the project repository.
