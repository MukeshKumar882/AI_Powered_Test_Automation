# Prompt: qTest-style test cases

Act as a QA test designer. Convert the reviewed scenarios in docs/test-scenarios.xlsx into qTest-style step rows using these columns: Test Case ID, Test Case Name, Requirement/AC, Priority, Preconditions, Test Data, Step No, Action, Expected Result, Test Type, Automation Candidate, Status. Use one row per test step, repeat case metadata on each row, quote CSV fields correctly, and do not add requirements. Return valid CSV only. Map the approved output to the qTest import template used by the team.

## Review record

The qTest-style workbook is a design/import aid. Verify field mappings against the qTest tenant’s current import template before import.
