# Contributing to 30 Days of Spring Boot & Microservices

Thank you for your interest in contributing to this project! This guide will help you get started.

## 📋 Table of Contents
- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Getting Started](#getting-started)
- [Contribution Guidelines](#contribution-guidelines)
- [Style Guidelines](#style-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

This project follows a Code of Conduct that all contributors are expected to adhere to. Please be respectful and constructive in all interactions.

## How Can I Contribute?

### Reporting Bugs

If you find a bug in the code examples or documentation:

1. **Check existing issues** to see if it's already reported
2. **Create a new issue** with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots (if applicable)
   - Your environment (OS, Java version, IDE)

### Suggesting Enhancements

We welcome suggestions for:
- New topics or content
- Improved explanations
- Better code examples
- Additional exercises
- More resources

Create an issue with the `enhancement` label and describe:
- What enhancement you'd like to see
- Why it would be useful
- How it could be implemented

### Improving Documentation

Documentation improvements are always welcome:
- Fix typos or grammatical errors
- Clarify confusing explanations
- Add missing information
- Improve code comments
- Add diagrams or illustrations

### Adding Code Examples

To contribute code examples:
1. Ensure code is well-tested
2. Follow Java best practices
3. Include comments explaining key concepts
4. Match the existing code style
5. Add corresponding documentation

### Translating Content

Help make this resource accessible to more people:
- Translate daily content to other languages
- Maintain consistency with original content
- Create a new language directory structure

## Getting Started

### Fork and Clone

```bash
# Fork the repository on GitHub
# Then clone your fork
git clone https://github.com/YOUR_USERNAME/30-Days-Java-Spring-Springboot.git
cd 30-Days-Java-Spring-Springboot

# Add upstream remote
git remote add upstream https://github.com/Abilash-K/30-Days-Java-Spring-Springboot.git
```

### Create a Branch

```bash
# Create a new branch for your contribution
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/your-bug-fix
```

### Make Your Changes

1. Make your changes in your branch
2. Test all code examples
3. Update documentation as needed
4. Add or update exercises if applicable

### Test Your Changes

```bash
# For code examples with Maven
cd DayXX_Topic/examples
mvn clean test

# For code examples with Gradle
cd DayXX_Topic/examples
./gradlew test
```

## Contribution Guidelines

### Content Structure

Each day should follow this structure:

```
DayXX_Topic/
├── README.md              # Main content
├── examples/              # Working code examples
│   └── *.java
├── exercises/             # Practice exercises
│   └── README.md
└── solutions/             # Exercise solutions
    └── *.java
```

### README.md Format

Each day's README should include:

```markdown
# Day XX: Topic Name

## 📋 Table of Contents
- [Introduction](#introduction)
- [Core Concepts](#core-concepts)
- [Code Examples](#code-examples)
- [Exercises](#exercises)
- [Resources](#resources)

## Introduction
Brief overview of the topic

## Core Concepts
Detailed explanations with examples

## Code Examples
Link to examples directory

## Exercises
Practice problems

## Resources
Additional learning materials

## Next Steps
Preview of next day

---
Navigation links
```

### Code Standards

**Java Code:**
```java
// Use descriptive names
public class UserService {
    
    // Include javadoc for public methods
    /**
     * Creates a new user in the system.
     *
     * @param user the user to create
     * @return the created user with generated ID
     * @throws UserAlreadyExistsException if user already exists
     */
    public User createUser(User user) {
        // Implementation
    }
    
    // Use proper exception handling
    // Follow SOLID principles
    // Keep methods focused and concise
}
```

**Spring Boot Configuration:**
```yaml
# Use consistent indentation (2 spaces)
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
    username: user
    password: pass
```

### Documentation Standards

- Use clear, concise language
- Include practical examples
- Explain the "why" not just the "what"
- Add diagrams for complex concepts
- Link to official documentation
- Keep content beginner-friendly

## Style Guidelines

### Markdown

- Use ATX-style headers (`#`, `##`, etc.)
- Include a blank line before and after headers
- Use code fences with language specification
- Use bullet points for lists
- Include links to related content

### Code Comments

```java
// Good: Explains why, not what
// Cache results to avoid repeated database calls
private Map<Long, User> userCache = new HashMap<>();

// Bad: States the obvious
// Create a new HashMap
private Map<Long, User> userCache = new HashMap<>();
```

### Naming Conventions

- Classes: `PascalCase`
- Methods: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `lowercase`
- Files: Match class name or use `kebab-case` for markdown

## Commit Message Guidelines

Follow conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature or content
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples

```
feat(day12): add microservices introduction

Added comprehensive microservices overview including:
- Architecture comparison
- Design patterns
- Communication patterns

Closes #42
```

```
fix(day05): correct REST API example

Fixed incorrect HTTP status code in error handling example.
Changed from 500 to 400 for validation errors.
```

```
docs(readme): update installation instructions

Updated JDK installation steps for Windows users.
Added troubleshooting section for common issues.
```

## Pull Request Process

### Before Submitting

1. **Update documentation** if you changed functionality
2. **Test all code** to ensure it works
3. **Check formatting** and style guidelines
4. **Update README** if you added new days or topics
5. **Sync with upstream** to avoid conflicts

```bash
# Sync with upstream
git fetch upstream
git rebase upstream/main
```

### Submit Pull Request

1. **Push your branch** to your fork
```bash
git push origin feature/your-feature-name
```

2. **Create Pull Request** on GitHub with:
   - Clear title describing the change
   - Detailed description of what and why
   - Link to related issues
   - Screenshots (if applicable)

3. **PR Description Template**
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] New content/feature
- [ ] Bug fix
- [ ] Documentation update
- [ ] Code improvement

## Checklist
- [ ] Code follows style guidelines
- [ ] All examples tested and working
- [ ] Documentation updated
- [ ] Self-review completed
- [ ] No merge conflicts

## Related Issues
Closes #XX
```

### Review Process

1. Maintainers will review your PR
2. Address any requested changes
3. Once approved, PR will be merged
4. Your contribution will be acknowledged

### After Merge

1. Delete your feature branch
```bash
git branch -d feature/your-feature-name
git push origin --delete feature/your-feature-name
```

2. Update your local repository
```bash
git checkout main
git pull upstream main
```

## Recognition

Contributors will be acknowledged in:
- README contributors section
- Individual day credits (for major contributions)
- Release notes

## Questions?

If you have questions about contributing:
1. Check existing issues and discussions
2. Create a new issue with the `question` label
3. Reach out to maintainers

## Thank You!

Your contributions help make this resource better for everyone learning Spring Boot and Microservices. We appreciate your time and effort! 🙏

---

**Happy Contributing!** 🚀
