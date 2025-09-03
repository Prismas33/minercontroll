# Contributing to MinerControl Android

We love your input! We want to make contributing to MinerControl Android as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## Development Process

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

### Pull Request Process

1. **Fork** the repository and create your branch from `master`
2. **Install** development dependencies and set up your environment
3. **Make** your changes and ensure the code compiles successfully
4. **Test** your changes thoroughly on real devices
5. **Update** documentation if you changed APIs
6. **Submit** a pull request with a clear description of changes

### Development Setup

```bash
# Clone your fork
git clone https://github.com/yourusername/minercontroll.git
cd minercontroll

# Create feature branch
git checkout -b feature/amazing-feature

# Build and test
./gradlew assembleDebug
./gradlew test
```

## Code Style

### Kotlin Guidelines
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **4 spaces** for indentation
- **Meaningful names** for variables and functions
- **Documentation** for public APIs

### Compose UI
- Use **Material 3** components when possible
- Follow **Compose best practices**
- Implement **proper state management**
- Ensure **accessibility** compliance

### Architecture
- Follow **MVVM pattern** with Repository pattern
- Use **Coroutines and Flow** for async operations
- Implement **proper error handling**
- Write **testable code**

## Bug Reports

We use GitHub issues to track public bugs. Report a bug by [opening a new issue](../../issues).

### Great Bug Reports Include:
- **Quick summary** and/or background
- **Steps to reproduce** (be specific!)
- **What you expected** would happen
- **What actually happens**
- **Device information** (Android version, device model)
- **Screenshots** if applicable

### Example Bug Report
```markdown
## Bug: Miners not appearing in dashboard

**Environment:**
- Device: Samsung Galaxy S21
- Android: 13
- App Version: 1.0.0

**Steps to Reproduce:**
1. Connect to WiFi network 192.168.1.x
2. Start MinerControl app  
3. Navigate to dashboard
4. Wait 30 seconds

**Expected:** Miners should appear in the list
**Actual:** Dashboard shows "No miners found"

**Additional Info:**
- Miners are sending UDP packets (verified with Wireshark)
- Port 12345 is configured correctly
- Network discovery works on desktop version
```

## Feature Requests

We welcome feature suggestions! Open an issue with:
- **Clear description** of the feature
- **Use case** explaining why it's needed
- **Mockups or examples** if applicable
- **Implementation ideas** (optional)

## Translations

Help us translate MinerControl to more languages:

1. **Check** existing translations in `app/src/main/kotlin/com/minercontrol/app/utils/LanguageManager.kt`
2. **Add** your language to `AppStrings` sealed class
3. **Translate** all string values
4. **Test** the translation in the app
5. **Submit** pull request

### Translation Guidelines
- Keep **consistent terminology**
- Consider **text length** (UI space constraints)
- Use **native conventions** for numbers and dates
- Test on **different screen sizes**

## Testing

### Manual Testing
- **Test on real devices** when possible
- **Different Android versions** (especially minimum API 24)
- **Various network configurations**
- **Different miner setups**

### Automated Testing
- **Unit tests** for business logic
- **UI tests** for critical user flows
- **Integration tests** for network communication

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests="MinerRepositoryTest"
```

## Code Review Process

All submissions, including submissions by project members, require review:

1. **Automated checks** must pass (build, tests, lint)
2. **Manual review** by maintainers
3. **Feedback** addressed and changes requested
4. **Approval** and merge when ready

### Review Criteria
- **Code quality** and style consistency
- **Performance** implications
- **Security** considerations
- **User experience** impact
- **Documentation** completeness

## Community Guidelines

### Be Respectful
- **Professional communication** in all interactions
- **Constructive feedback** only
- **Inclusive language** and behavior
- **Help newcomers** get started

### Stay On Topic
- Keep discussions **relevant to MinerControl**
- Use **appropriate channels** (issues vs discussions)
- **Search existing** issues before creating new ones

## Recognition

Contributors will be recognized in:
- **README acknowledgments**
- **Release notes** for significant contributions
- **Contributors section** on GitHub

## Getting Help

- **Documentation**: Check README and inline code comments
- **Issues**: Search existing issues for similar problems
- **Discussions**: Use GitHub Discussions for general questions
- **Direct Contact**: Maintainers available for complex issues

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Thank you for contributing to MinerControl Android!** 🚀

Every contribution helps make cryptocurrency mining more accessible and efficient for everyone.
