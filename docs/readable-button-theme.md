# High-contrast button and sidebar theme

The UI uses explicit button colors rather than relying on the operating-system look and feel to choose text colors.

- Primary blue action buttons use a dark blue fill with white text.
- Sidebar navigation uses near-white text on a dark navy background; the current page is highlighted in blue with a pale-blue indicator bar.
- Sidebar hover and pressed states keep white text on dark backgrounds, so labels remain readable while interacting.
- Sign-out uses the same high-contrast sidebar treatment.
- Secondary actions use dark text on pale backgrounds; destructive actions use dark red text on a pale red surface.
- Disabled action buttons use a pale neutral background and dark text.
- The login page displays empty username/password fields and no demo credentials under the fields. Demo credentials remain in the README for setup/testing only.

The four `ui-preview-*.png` images are interface previews. Runtime behavior still needs to be tested in NetBeans with Apache Derby.
