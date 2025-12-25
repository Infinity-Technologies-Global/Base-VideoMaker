#!/bin/bash

# Script to prepare and push to GitHub

echo "🚀 Preparing to push to GitHub..."

# Check if git is initialized
if [ ! -d ".git" ]; then
    echo "❌ Git not initialized. Run: git init"
    exit 1
fi

# Sync markdown files to docs_html
echo "📄 Syncing markdown files..."
cd docs_html
./sync_markdown.sh
cd ..

# Add all files
echo "📦 Adding files to git..."
git add docs_html/
git add *.md
git add DEPLOY_GUIDE.md
git add app/src/main/java/com/ynsuper/slideshowver1/util/PermissionHelper.kt

# Show status
echo ""
echo "📋 Files to be committed:"
git status --short

echo ""
read -p "Continue with commit? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Cancelled"
    exit 1
fi

# Commit
echo "💾 Committing..."
git commit -m "docs: Add architecture documentation and HTML viewer

- Add multi-track timeline architecture (CapCut-style)
- Add ExoPlayer GL compositing architecture
- Add real-time preview explanation
- Add timeline layers documentation
- Add HTML documentation viewer with dark theme
- Add PermissionHelper utility for audio permissions
- Fix TedImagePicker callback issue with static holder
- Support image + video mixing in slideshow"

# Check remote
echo ""
echo "🌐 Checking remotes..."
git remote -v

echo ""
read -p "Push to GitHub? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "✅ Committed. Push manually with: git push"
    exit 0
fi

# Push
BRANCH=$(git branch --show-current)
echo "📤 Pushing to GitHub (branch: $BRANCH)..."

# Try github remote first
if git remote | grep -q "^github$"; then
    git push github $BRANCH
elif git remote | grep -q "^origin$"; then
    echo "⚠️  No 'github' remote found. Pushing to 'origin' instead."
    echo "💡 To add GitHub remote: git remote add github https://github.com/USERNAME/REPO.git"
    git push origin $BRANCH
else
    echo "❌ No remote found. Please add remote first:"
    echo "   git remote add github https://github.com/USERNAME/REPO.git"
    exit 1
fi

echo ""
echo "✅ Done! Now deploy to Vercel:"
echo "   1. Go to https://vercel.com"
echo "   2. Import project from GitHub"
echo "   3. Set Root Directory = docs_html"
echo "   4. Deploy!"

