const express = require('express');
const cors = require('cors');
const { sequelize } = require('./config/database');
const authRoutes = require('./routes/auth');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json());

app.use('/api/auth', authRoutes);

app.get('/about.json', (req, res) => {
    res.json({
        client: {
            host: req.ip
        },
        server: {
            current_time: Math.floor(Date.now() / 1000),
            services: [
                {
                    name: "express",
                    description: "Node.js web framework",
                    actions: [
                        {
                            name: "user_registered",
                            description: "A new user registers with Express"
                        }
                    ],
                    reactions: [
                        {
                            name: "save_user",
                            description: "Save user data with Sequelize"
                        }
                    ]
                }
            ]
        }
    });
});

sequelize.sync({ alter: true }).then(() => {
    app.listen(PORT, () => {
        console.log(`Server running on port ${PORT}`);
    });
}).catch(err => {
    console.error('Unable to connect to the database:', err);
});
